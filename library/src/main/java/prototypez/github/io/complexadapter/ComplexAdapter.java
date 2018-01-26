package prototypez.github.io.complexadapter;

import android.support.annotation.MainThread;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by zhounl on 2018/1/22.
 */

public class ComplexAdapter extends RecyclerView.Adapter {


    private BehaviorSubject<ComputationAdapter> computationAdapterSubject = BehaviorSubject.create();

    private PublishSubject<Object> refreshSignal = PublishSubject.create();

    public ComplexAdapter(List<SubAdapter> subAdapters, List<Integer> subAdapterTypes) {

        List<ComputationAdapter.Section> sections = new ArrayList<>();
        for (int i = 0; i < subAdapters.size(); i++) {
            ComputationAdapter.Section section = new ComputationAdapter.Section(
                    i,
                    new ArrayList<>()
            );
            sections.add(section);
        }

        computationAdapterSubject.onNext(new ComputationAdapter(subAdapters, sections, subAdapterTypes));

        refreshSignal
                .debounce(300, TimeUnit.MILLISECONDS)
                .withLatestFrom(computationAdapterSubject, (o, computationAdapter) -> computationAdapter)
                .flatMap(ComputationAdapter::refresh)
                .observeOn(Schedulers.single())
                .withLatestFrom(computationAdapterSubject, (sectionListPair, computationAdapter) -> {
                    // 根据上一次的 computationAdapter 以及本次更新的 subAdapter 数据，生成新的 computationAdapter
                    // 同时计算新生成的与老的之间的 diff
                    ComputationAdapter.Section updatedSection = sectionListPair.first;
                    List<AdapterItem> updatedList = sectionListPair.second;

                    List<ComputationAdapter.Section> currentSections = computationAdapter.copySections();
                    currentSections.get(updatedSection.index).adapterData = updatedList;

                    ComputationAdapter currentAdapter = new ComputationAdapter(
                            computationAdapter.getSubAdapters(),
                            currentSections,
                            subAdapterTypes
                    );

                    computationAdapterSubject.onNext(currentAdapter);

                    return new ComputationResult(
                            currentAdapter.compare(computationAdapter),
                            currentAdapter
                    );
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(computationResult -> updateViewData(computationResult.mDiffResult, computationResult.mComputationAdapter));
    }


    public void refresh() {
        refreshSignal.onNext("");
    }

    private ComputationAdapter mComputationAdapter;

    @MainThread
    private void updateViewData(DiffUtil.DiffResult diffResult, ComputationAdapter computationAdapter) {
        mComputationAdapter = computationAdapter;
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        return mComputationAdapter.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mComputationAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mComputationAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        if (mComputationAdapter == null) {
            return 0;
        } else {
            return mComputationAdapter.getItemCount();
        }
    }

    class ComputationResult {
        DiffUtil.DiffResult mDiffResult;
        ComputationAdapter mComputationAdapter;

        ComputationResult(DiffUtil.DiffResult diffResult, ComputationAdapter computationAdapter) {
            mDiffResult = diffResult;
            mComputationAdapter = computationAdapter;
        }
    }
}
