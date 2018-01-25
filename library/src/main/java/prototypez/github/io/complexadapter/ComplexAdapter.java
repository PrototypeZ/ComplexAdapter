package prototypez.github.io.complexadapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.annotation.MainThread;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator;
import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;
import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;
import jp.wasabeef.recyclerview.animators.FlipInBottomXAnimator;
import jp.wasabeef.recyclerview.animators.FlipInLeftYAnimator;
import jp.wasabeef.recyclerview.animators.FlipInRightYAnimator;
import jp.wasabeef.recyclerview.animators.FlipInTopXAnimator;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import jp.wasabeef.recyclerview.animators.OvershootInLeftAnimator;
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInRightAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInTopAnimator;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import jp.wasabeef.recyclerview.internal.ViewHelper;

/**
 * Created by zhounl on 2018/1/22.
 */

public class ComplexAdapter extends RecyclerView.Adapter {


    private BehaviorSubject<ComputationAdapter> computationAdapterSubject = BehaviorSubject.create();

    private PublishSubject<Object> refreshSignal = PublishSubject.create();

    private RecyclerView mRecyclerView;

    private int mLastPosition = -1;

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
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        RecyclerView.ItemAnimator animator = getItemAnimator();
        if (animator != null) {
            mRecyclerView.setItemAnimator(animator);
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

        int adapterPosition = holder.getAdapterPosition();

        Animator anim = getAdapterAnimator(holder.itemView, position);

        if (anim != null && adapterPosition > mLastPosition) {
            anim.start();
            mLastPosition = adapterPosition;
        } else {
            ViewHelper.clear(holder.itemView);
        }
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

    /**
     * RecyclerView Item 进入动画
     */
    protected Animator getAdapterAnimator(View itemView, int position) {
        Animator animator = ObjectAnimator.ofFloat(itemView, "translationY", itemView.getMeasuredHeight(), 0)
                .setDuration(500);
        animator.setInterpolator(new OvershootInterpolator(.5f));
        return animator;
    }

    protected RecyclerView.ItemAnimator getItemAnimator() {
        RecyclerView.ItemAnimator animator = BuiltInAnimatorType.getAnimatorSlideInRight();
        animator.setAddDuration(500);
        animator.setRemoveDuration(500);
        return animator;
    }

    protected static class BuiltInAnimatorType {
        public static RecyclerView.ItemAnimator getAnimatorFadeIn() {
            return new FadeInAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorFadeInDown() {
            return new FadeInDownAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorFadeInUp() {
            return new FadeInUpAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorFadeInLeft() {
            return new FadeInLeftAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorFadeInRight() {
            return new FadeInRightAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorLanding() {
            return new LandingAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorScaleIn() {
            return new ScaleInAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorScaleInTop() {
            return new ScaleInTopAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorScaleInBottom() {
            return new ScaleInBottomAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorScaleInLeft() {
            return new ScaleInLeftAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorScaleInRight() {
            return new ScaleInRightAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorFlipInTopX() {
            return new FlipInTopXAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorFlipInBottomX() {
            return new FlipInBottomXAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorFlipInLeftY() {
            return new FlipInLeftYAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorFlipInRightY() {
            return new FlipInRightYAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorSlideInLeft() {
            return new SlideInLeftAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorSlideInRight() {
            return new SlideInRightAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorSlideInDown() {
            return new SlideInDownAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorSlideInUp() {
            return new SlideInUpAnimator();
        }

        public static RecyclerView.ItemAnimator getAnimatorOvershootInRight() {
            return new OvershootInRightAnimator(1.0f);
        }

        public static RecyclerView.ItemAnimator getAnimatorOvershootInLeft() {
            return new OvershootInLeftAnimator(1.0f);
        }
    }
}
