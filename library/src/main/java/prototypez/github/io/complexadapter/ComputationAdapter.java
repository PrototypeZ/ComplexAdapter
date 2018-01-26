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
        List<AdapterItem> adapterData;

        Section(int index, List<AdapterItem> adapterData) {
            this.index = index;
            this.adapterData = adapterData;
        }
    }

    private List<SubAdapter> mSubAdapters = new ArrayList<>();

    private List<Section> mSections = new ArrayList<>();

    private List<Integer> mTypes = new ArrayList<>();

    private Map<SubAdapter, Integer> subAdapterTypeMap = new HashMap<>();

    private SparseArray<SubAdapter> typeSubAdapterMap = new SparseArray<>();

    DiffUtil.DiffResult compare(ComputationAdapter oldAdapter) {
        return DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldAdapter.getItemCount();
            }

            @Override
            public int getNewListSize() {
                return getItemCount();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                if (oldAdapter.getItemViewType(oldItemPosition) != getItemViewType(newItemPosition)) {
                    return false;
                } else {
                    Object oldRepresentObj = oldAdapter.getRepresentObjectAt(oldItemPosition);
                    Object newRepresentObj = getRepresentObjectAt(newItemPosition);
                    if (oldRepresentObj instanceof AdapterItem && newRepresentObj instanceof AdapterItem) {
                        return ((AdapterItem) oldRepresentObj).isItemTheSameTo((AdapterItem) newRepresentObj);
                    } else
                        return !(oldRepresentObj instanceof AdapterItem)
                                && !(newRepresentObj instanceof AdapterItem)
                                && oldRepresentObj.equals(newRepresentObj);
                }
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                if (oldAdapter.getItemViewType(oldItemPosition) != getItemViewType(newItemPosition)) {
                    return false;
                } else {
                    Object oldRepresentObj = oldAdapter.getRepresentObjectAt(oldItemPosition);
                    Object newRepresentObj = getRepresentObjectAt(newItemPosition);
                    if (oldRepresentObj instanceof AdapterItem && newRepresentObj instanceof AdapterItem) {
                        return ((AdapterItem) oldRepresentObj).isContentTheSameTo((AdapterItem) newRepresentObj);
                    } else
                        return !(oldRepresentObj instanceof AdapterItem)
                                && !(newRepresentObj instanceof AdapterItem)
                                && oldRepresentObj.equals(newRepresentObj);
                }
            }
        });
    }

    ComputationAdapter(List<SubAdapter> subAdapters, List<Section> sections, List<Integer> subAdapterTypes) {

        mSubAdapters = subAdapters;
        mSections = sections;
        mTypes = subAdapterTypes;

        for (int i = 0; i < subAdapters.size(); i++) {
            SubAdapter subAdapter = subAdapters.get(i);
            subAdapterTypeMap.put(subAdapter, subAdapterTypes.get(i));
            typeSubAdapterMap.put(subAdapterTypes.get(i), subAdapter);
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

    List<Integer> getAdapterTypes() {
        return mTypes;
    }

    List<Section> createNewSectionsFromNewSubAdapters(List<SubAdapter> subAdapters) {
        List<Section> sections = new ArrayList<>();
        for (int i = 0; i < subAdapters.size(); i++) {
            int indexInOldSubAdapters = mSubAdapters.indexOf(subAdapters.get(i));
            Section section;
            if (indexInOldSubAdapters != -1) {
                List<AdapterItem> adapterItems = new ArrayList<>();
                adapterItems.addAll(mSections.get(indexInOldSubAdapters).adapterData);
                section = new Section(i, adapterItems);
            } else {
                section = new Section(i, new ArrayList<>());
            }
            sections.add(section);
        }
        return sections;
    }

    List<Section> copySections() {
        List<Section> sections = new ArrayList<>(mSections.size());
        for (int i = 0; i < mSections.size(); i++) {
            Section oldSection = mSections.get(i);
            List<AdapterItem> adapterItems = new ArrayList<>();
            adapterItems.addAll(oldSection.adapterData);
            Section section = new Section(oldSection.index, adapterItems);
            sections.add(section);
        }
        return sections;
    }

    @Override
    public int getItemViewType(int position) {
        SubAdapterInfo subAdapterInfo = findSubAdapterInfoByPosition(position);
        int globalTypeOffset = subAdapterTypeMap.get(subAdapterInfo.subAdapter);
        return globalTypeOffset * 1000 + subAdapterInfo.subAdapter.getItemViewTypeInside(
                mSections.get(subAdapterInfo.adapterIndex).adapterData,
                subAdapterInfo.relativePosition
        );
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int innerViewType = viewType % 1000;
        SubAdapterInfo subAdapterInfo = findSubAdapterInfoByViewType(viewType);
        return subAdapterInfo.subAdapter.onCreateViewHolderInside(
                mSections.get(subAdapterInfo.adapterIndex).adapterData,
                parent,
                innerViewType
        );
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SubAdapterInfo subAdapterInfo = findSubAdapterInfoByPosition(position);
        subAdapterInfo.subAdapter.onBindViewHolderInside(
                mSections.get(subAdapterInfo.adapterIndex).adapterData,
                holder,
                subAdapterInfo.relativePosition
        );
    }

    public Object getRepresentObjectAt(int position) {
        SubAdapterInfo subAdapterInfo = findSubAdapterInfoByPosition(position);
        return subAdapterInfo.subAdapter.getRepresentObjectAt(
                mSections.get(subAdapterInfo.adapterIndex).adapterData,
                subAdapterInfo.relativePosition
        );
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (int i = 0; i < mSubAdapters.size(); i++) {
            count += mSubAdapters.get(i).getItemCount(mSections.get(i).adapterData);
        }
        return count;
    }

    private SubAdapterInfo findSubAdapterInfoByPosition(int position) {
        int cursor = 0;
        for (int i = 0; i < mSubAdapters.size(); i++) {
            SubAdapter subAdapter = mSubAdapters.get(i);
            Section section = mSections.get(i);
            int size = subAdapter.getItemCount(section.adapterData);
            if (cursor <= position && position < cursor + size) {
                return new SubAdapterInfo(mSubAdapters.get(i), position - cursor, i);
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

        return new SubAdapterInfo(subAdapter, 0, index);
    }

    class SubAdapterInfo {
        SubAdapter subAdapter;
        int relativePosition;
        int adapterIndex;

        SubAdapterInfo(SubAdapter subAdapter, int relativePosition, int adapterIndex) {
            this.subAdapter = subAdapter;
            this.relativePosition = relativePosition;
            this.adapterIndex = adapterIndex;
        }
    }
}
