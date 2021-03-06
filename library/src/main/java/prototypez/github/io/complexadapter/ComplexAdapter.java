package prototypez.github.io.complexadapter;

import android.support.annotation.MainThread;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by zhounl on 2018/1/22.
 */

public class ComplexAdapter extends RecyclerView.Adapter {


    private PublishSubject<ComputationAdapter> computationAdapterSubject = PublishSubject.create();

    private PublishSubject<Object> refreshSignal = PublishSubject.create();

    private Observable<Pair<List<SubAdapter>, List<Integer>>> subAdapterAndTypes;

    public ComplexAdapter() {

        PublishSubject<ComputationResult> computationResultSubject = PublishSubject.create();

        Observable<ComputationAdapter> seqRefreshObservable = refreshSignal
                .debounce(300, TimeUnit.MILLISECONDS)
                .switchMap(o -> subAdapterAndTypes)
                .observeOn(Schedulers.single())
                .withLatestFrom(computationAdapterSubject.startWith(new ComputationAdapter(new ArrayList<>(), new ArrayList<>(), new ArrayList<>())), (pair, computationAdapter) -> {
                    Log.i("ComplexAdapter", "[" + Thread.currentThread() + "]" + "receive adapter sequence:" + pair);
                    // 获得最新的 subAdapter 顺序，根据顺序重排 subAdapter
                    List<SubAdapter> newSubAdapters = pair.first;
                    List<Integer> newSubAdapterTypes = pair.second;
                    List<ComputationAdapter.Section> newSections =
                            computationAdapter.createNewSectionsFromNewSubAdapters(newSubAdapters);

                    // 重排后新的 Adapter
                    ComputationAdapter newComputationAdapter = new ComputationAdapter(
                            newSubAdapters,
                            newSections,
                            newSubAdapterTypes
                    );

                    // 重排后，和老的 Adapter 做 Diff，通知视图
                    computationResultSubject.onNext(
                            new ComputationResult(
                                    newComputationAdapter.compare(computationAdapter),
                                    newComputationAdapter
                            )
                    );

                    // 更新最新 Adapter 流
                    computationAdapterSubject.onNext(newComputationAdapter);
                    Log.i("ComplexAdapter", "[" + Thread.currentThread() + "]" + "calculate adapter sequence change ok.");
                    return newComputationAdapter;
                });

        Observable<ComputationResult> subAdapterRefreshObservable = seqRefreshObservable
                .switchMap(computationAdapter -> computationAdapter.refresh().takeUntil(seqRefreshObservable))
                .observeOn(Schedulers.single())
                .withLatestFrom(computationAdapterSubject, (subAdapterRefreshResult, computationAdapter) -> {
                    Log.i("ComplexAdapter", "[" + Thread.currentThread() + "]" + "receive subAdapter refresh:" + subAdapterRefreshResult + subAdapterRefreshResult.types);
                    // 根据上一次的 computationAdapter 以及本次更新的 subAdapter 数据，生成新的 computationAdapter
                    // 同时计算新生成的与老的之间的 diff
                    ComputationAdapter.Section updatedSection = subAdapterRefreshResult.refreshedSection;
                    Object updatedData = subAdapterRefreshResult.data;

                    if (subAdapterRefreshResult.types.equals(computationAdapter.getAdapterTypes())) {

                        List<ComputationAdapter.Section> currentSections = computationAdapter.copySections();
                        currentSections.get(updatedSection.index).adapterData = updatedData;

                        ComputationAdapter currentAdapter = new ComputationAdapter(
                                computationAdapter.getSubAdapters(),
                                currentSections,
                                computationAdapter.getAdapterTypes()
                        );

                        computationAdapterSubject.onNext(currentAdapter);
                        Log.i("ComplexAdapter", "[" + Thread.currentThread() + "]" + "calculate subAdapter refresh ok.");
                        return new ComputationResult(
                                currentAdapter.compare(computationAdapter),
                                currentAdapter
                        );
                    } else {
                        return new ComputationResult(null, computationAdapter);
                    }
                });

        subAdapterRefreshObservable
                .subscribe(computationResultSubject::onNext);

        computationResultSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(computationResult -> updateViewData(computationResult.mDiffResult, computationResult.mComputationAdapter));
    }

    public void setRefreshAdapterOrderObservable(Observable<Pair<List<SubAdapter>, List<Integer>>> subAdapterAndTypes) {
        this.subAdapterAndTypes = subAdapterAndTypes;
    }


    public void refresh() {
        refreshSignal.onNext("");
    }

    private ComputationAdapter mComputationAdapter;

    @MainThread
    private void updateViewData(DiffUtil.DiffResult diffResult, ComputationAdapter computationAdapter) {
        Log.i("ComplexAdapter", "[" + Thread.currentThread() + "]" + "do ui refresh:" + diffResult);
        if (diffResult != null) {
            mComputationAdapter = computationAdapter;
            diffResult.dispatchUpdatesTo(this);
        }
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
