package prototypez.github.io.complexadapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import io.reactivex.Observable;

/**
 * Created by zhounl on 2018/1/22.
 */

public interface SubAdapter<T, VH extends RecyclerView.ViewHolder> {

    Observable<T> refreshData();

    int getItemCount(T data);

    /**
     *
     * @param data
     * @param position
     * @return 0 <= itemType< 999
     */
    default int getItemViewTypeInside(T data, int position) {
        return 0;
    }

    VH onCreateViewHolderInside(T data, ViewGroup parent, int viewType);

    void onBindViewHolderInside(T data, VH holder, int position);

    Object getRepresentObjectAt(T data, int position);
}
