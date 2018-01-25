package prototypez.github.io.complexadapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by zhounl on 2018/1/22.
 */

public interface SubAdapter<T extends AdapterItem, VH extends RecyclerView.ViewHolder> {

    Observable<List<T>> refreshData();

    int getItemCount(List<T> data);

    /**
     *
     * @param data
     * @param position
     * @return 0 <= itemType< 999
     */
    default int getItemViewTypeInside(List<T> data, int position) {
        return 0;
    }

    VH onCreateViewHolderInside(List<T> data, ViewGroup parent, int viewType);

    void onBindViewHolderInside(List<T> data, VH holder, int position);

    Object getRepresentObjectAt(List<T> data, int position);
}
