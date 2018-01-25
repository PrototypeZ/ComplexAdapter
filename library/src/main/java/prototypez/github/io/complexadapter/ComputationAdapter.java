package prototypez.github.io.complexadapter;

import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Created by zhounl on 2018/1/24.
 */

public class ComputationAdapter extends RecyclerView.Adapter {

    static class Section {
        int index;
        // Assume 每个 section adapterClass 都不一样
        Class adapterClass;
        List<AdapterItem> adapterData;

        Section(int index, Class adapterClass, List<AdapterItem> adapterData) {
            this.index = index;
            this.adapterClass = adapterClass;
            this.adapterData = adapterData;
        }
    }

    private List<SubAdapter> mSubAdapters = new ArrayList<>();

    private List<Section> mSections = new ArrayList<>();

    private Map<SubAdapter, Integer> subAdapterTypeMap = new HashMap<>();

    private SparseArray<SubAdapter> typeSubAdapterMap = new SparseArray<>();

    private List<AdapterItem> viewData = new ArrayList<>();

    DiffUtil.DiffResult compare(ComputationAdapter oldAdapter) {
        return DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldAdapter.viewData.size();
            }

            @Override
            public int getNewListSize() {
                return viewData.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                int oldAdapterType = oldAdapter.getItemViewType(oldItemPosition);
                int newAdapterType = getItemViewType(newItemPosition);
                return oldAdapterType == newAdapterType && oldAdapter.viewData.get(oldItemPosition).isItemTheSameTo(viewData.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                int oldAdapterType = oldAdapter.getItemViewType(oldItemPosition);
                int newAdapterType = getItemViewType(newItemPosition);
                return oldAdapterType == newAdapterType && oldAdapter.viewData.get(oldItemPosition).isContentTheSameTo(viewData.get(newItemPosition));
            }
        });
    }

    ComputationAdapter(List<SubAdapter> subAdapters, List<Section> sections, List<Integer> subAdapterTypes) {

        mSubAdapters = subAdapters;
        mSections = sections;

        for (int i = 0; i < subAdapters.size(); i++) {
            SubAdapter subAdapter = subAdapters.get(i);
            subAdapterTypeMap.put(subAdapter, subAdapterTypes.get(i));
            typeSubAdapterMap.put(subAdapterTypes.get(i), subAdapter);
            viewData.addAll(sections.get(i).adapterData);
        }
    }

    Observable<Pair<Section, List<AdapterItem>>> refresh() {
        Observable result = Observable.empty();
        for (int i = 0; i < mSections.size(); i++) {
            result = result.mergeWith(
                    Observable.combineLatest(
                            Observable.just(mSections.get(i)),
                            mSubAdapters.get(i).refreshData(),
                            (section, list) -> Pair.create(section, list)
                    )
            );
        }
        return result;
    }

    List<SubAdapter> getSubAdapters() {
        return mSubAdapters;
    }

    List<Section> copySections() {
        List<Section> sections = new ArrayList<>(mSections.size());
        for (int i = 0; i < mSections.size(); i++) {
            Section oldSection = mSections.get(i);
            List<AdapterItem> adapterItems = new ArrayList<>();
            adapterItems.addAll(oldSection.adapterData);
            Section section = new Section(oldSection.index, oldSection.adapterClass, adapterItems);
            sections.add(section);
        }
        return sections;
    }

    @Override
    public int getItemViewType(int position) {
        SubAdapterInfo subAdapterInfo = findSubAdapterInfoByPosition(position);
        int globalTypeOffset = subAdapterTypeMap.get(subAdapterInfo.subAdapter);
        return globalTypeOffset * 1000 + subAdapterInfo.subAdapter.getItemViewTypeInside(
                viewData.subList(subAdapterInfo.sectionStartIndex, subAdapterInfo.sectionStartIndex + subAdapterInfo.size),
                subAdapterInfo.relativePosition
        );
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int innerViewType = viewType % 1000;
        SubAdapterInfo subAdapterInfo = findSubAdapterInfoByViewType(viewType);
        return subAdapterInfo.subAdapter.onCreateViewHolderInside(
                viewData.subList(subAdapterInfo.sectionStartIndex, subAdapterInfo.sectionStartIndex + subAdapterInfo.size),
                parent,
                innerViewType
        );
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SubAdapterInfo subAdapterInfo = findSubAdapterInfoByPosition(position);
        subAdapterInfo.subAdapter.onBindViewHolderInside(
                viewData.subList(subAdapterInfo.sectionStartIndex, subAdapterInfo.sectionStartIndex + subAdapterInfo.size),
                holder,
                subAdapterInfo.relativePosition
        );
    }

    @Override
    public int getItemCount() {
        return viewData.size();
    }

    private SubAdapterInfo findSubAdapterInfoByPosition(int position) {
        int cursor = 0;
        for (int i = 0; i < mSections.size(); i++) {
            Section section = mSections.get(i);
            int size = section.adapterData.size();
            if (cursor <= position && position < cursor + size) {
                return new SubAdapterInfo(mSubAdapters.get(i), position - cursor, cursor, size, i);
            } else {
                cursor += size;
            }
        }
        return null;
    }

    private SubAdapterInfo findSubAdapterInfoByViewType(int viewType) {
        int globalViewType = viewType / 1000;
        SubAdapter subAdapter = typeSubAdapterMap.get(globalViewType);
        int index = mSubAdapters.indexOf(subAdapter);
        int start = 0;
        for (int i = 0; i < index; i++) {
            start += mSections.get(i).adapterData.size();
        }

        return new SubAdapterInfo(subAdapter, 0, start, mSections.get(index).adapterData.size(), index);
    }

    class SubAdapterInfo {
        SubAdapter subAdapter;
        int relativePosition;
        int sectionStartIndex;
        int size;
        int adapterIndex;

        SubAdapterInfo(SubAdapter subAdapter, int relativePosition, int sectionStartIndex, int size, int adapterIndex) {
            this.subAdapter = subAdapter;
            this.relativePosition = relativePosition;
            this.sectionStartIndex = sectionStartIndex;
            this.size = size;
            this.adapterIndex = adapterIndex;
        }
    }
}
