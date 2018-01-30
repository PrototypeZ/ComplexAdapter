package prototypez.github.io.complexadapter.demo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import prototypez.github.io.complexadapter.AdapterItem;
import prototypez.github.io.complexadapter.ComplexAdapter;
import prototypez.github.io.complexadapter.SubAdapter;
import prototypez.github.io.complexadapter.demo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

//        mBinding.rv1.setAdapter(new Adapter1(Arrays.asList(new Entity("a"), new Entity("b"))));
//        mBinding.rv1.setLayoutManager(new LinearLayoutManager(this));
//        mBinding.rv2.setAdapter(new Adapter2(Arrays.asList(new Entity("c"), new Entity("d"))));
//        mBinding.rv2.setLayoutManager(new LinearLayoutManager(this));

        SubAdapter subAdapter3 = new Adapter3();
        SubAdapter subAdapter4 = new Adapter4();

        ComplexAdapter adapter = new ComplexAdapter();

        adapter.setRefreshAdapterOrderObservable(
                Observable
                        .just(
                                Pair.create(
                                        Arrays.asList(subAdapter4, subAdapter3),
                                        Arrays.asList(2, 1)
                                )
                        )
                        .delay(7000, TimeUnit.MILLISECONDS)
                        .startWith(
                                Observable
                                        .just(
                                                Pair.create(
                                                        Arrays.asList(subAdapter3, subAdapter4),
                                                        Arrays.asList(1, 2)
                                                )
                                        )
                        )
        );

        RecyclerView.ItemAnimator animator = new CustomFadeInAnimator();
        animator.setAddDuration(1000);
        animator.setRemoveDuration(1000);

        mBinding.rv1.setAdapter(adapter);
        mBinding.rv1.setItemAnimator(animator);
        mBinding.rv1.setLayoutManager(new LinearLayoutManager(this));

        mBinding.refresh.setOnClickListener(v -> adapter.refresh());
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv;

        ViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }
    }

    class Response {
        int rc;
        List<Entity> content;

        Response(int rc, List<Entity> content) {
            this.rc = rc;
            this.content = content;
        }
    }

    class Entity implements AdapterItem {

        String data;

        Entity(String data) {
            this.data = data;
        }

        @Override
        public boolean isItemTheSameTo(AdapterItem adapterItem) {
            return adapterItem instanceof Entity && ((Entity) adapterItem).data.equals(data);
        }

        @Override
        public boolean isContentTheSameTo(AdapterItem adapterItem) {
            return adapterItem instanceof Entity && ((Entity) adapterItem).data.equals(data);
        }
    }

//    class Adapter1 extends RecyclerView.Adapter<ViewHolder> implements SubAdapter<Entity, ViewHolder> {
//
//        List<Entity> mEntities;
//
//        Adapter1(List<Entity> entities) {
//            mEntities = entities;
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            return onCreateViewHolderInside(mEntities, parent, viewType);
//        }
//
//        @Override
//        public void onBindViewHolder(ViewHolder holder, int position) {
//            onBindViewHolderInside(mEntities, holder, position);
//        }
//
//        @Override
//        public int getItemCount() {
//            return mEntities.size();
//        }
//
//        // SubAdapter implementation
//
//        @Override
//        public Observable<List<Entity>> refreshData() {
//            return Observable
//                    .just(Arrays.asList(new Entity("1"), new Entity("2"), new Entity("3")))
//                    .delay(500, TimeUnit.MILLISECONDS)
//                    .observeOn(Schedulers.io());
//        }
//
//        @Override
//        public int getItemCount(List<Entity> data) {
//            return data.size();
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolderInside(List<Entity> data, ViewGroup parent, int viewType) {
//            return new ViewHolder(
//                    LayoutInflater.from(parent.getContext()).inflate(
//                            R.layout.layout_rv1_item,
//                            parent,
//                            false
//                    )
//            );
//        }
//
//        @Override
//        public void onBindViewHolderInside(List<Entity> data, ViewHolder holder, int position) {
//            holder.tv.setText(data.get(position).data);
//        }
//
//        @Override
//        public Object getRepresentObjectAt(List<Entity> data, int position) {
//            return data.get(position);
//        }
//    }
//
//
//    class Adapter2 extends RecyclerView.Adapter<ViewHolder> implements SubAdapter<Entity, ViewHolder> {
//
//        List<Entity> mEntities;
//
//        Adapter2(List<Entity> entities) {
//            mEntities = entities;
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            return onCreateViewHolderInside(mEntities, parent, viewType);
//        }
//
//        @Override
//        public void onBindViewHolder(ViewHolder holder, int position) {
//            onBindViewHolderInside(mEntities, holder, position);
//        }
//
//        @Override
//        public int getItemCount() {
//            return mEntities.size();
//        }
//
//        // SubAdapter implementation
//
//        @Override
//        public Observable<List<Entity>> refreshData() {
//            return Observable
//                    .just(Arrays.asList(new Entity("1"), new Entity("2"), new Entity("3"), new Entity("4")))
//                    .delay(1000, TimeUnit.MILLISECONDS)
//                    .observeOn(Schedulers.io());
//        }
//
//        @Override
//        public int getItemCount(List<Entity> data) {
//            return data.size();
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolderInside(List<Entity> data, ViewGroup parent, int viewType) {
//            return new ViewHolder(
//                    LayoutInflater.from(parent.getContext()).inflate(
//                            R.layout.layout_rv2_item,
//                            parent,
//                            false
//                    )
//            );
//        }
//
//        @Override
//        public void onBindViewHolderInside(List<Entity> data, ViewHolder holder, int position) {
//            holder.tv.setText(data.get(position).data);
//        }
//
//        @Override
//        public Object getRepresentObjectAt(List<Entity> data, int position) {
//            return data.get(position);
//        }
//    }


    class Adapter3 extends RecyclerView.Adapter<ViewHolder> implements SubAdapter<Response, ViewHolder> {

        Response mResponse;

        private static final int CONTENT = 0;
        private static final int HEADER = 1;
        private static final int BOTTOM = 2;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return onCreateViewHolderInside(mResponse, parent, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            onBindViewHolderInside(mResponse, holder, position);
        }

        @Override
        public int getItemCount() {
            return getItemCount(mResponse);
        }

        // SubAdapter implementation

        @Override
        public Observable<Response> refreshData() {
            return Observable
                    .just(
                            new Response(
                                    200,
                                    Arrays.asList(
                                            new Entity("5"), new Entity("6"), new Entity("7"), new Entity("8")
                                    )
                            )
                    )
                    .delay(500, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.io());
        }

        @Override
        public int getItemCount(Response data) {
            if (data != null && data.content != null && data.content.size() > 0) {
                return data.content.size() + 2;
            } else {
                return 0;
            }
        }

        @Override
        public int getItemViewTypeInside(Response data, int position) {
            if (position == 0) {
                return HEADER;
            } else if (position < data.content.size() + 1) {
                return CONTENT;
            } else {
                return BOTTOM;
            }
        }

        @Override
        public ViewHolder onCreateViewHolderInside(Response data, ViewGroup parent, int viewType) {
            switch (viewType) {
                case HEADER:
                    return new ViewHolder(
                            LayoutInflater.from(parent.getContext()).inflate(
                                    R.layout.layout_item_header,
                                    parent,
                                    false
                            )
                    );
                case CONTENT:
                    return new ViewHolder(
                            LayoutInflater.from(parent.getContext()).inflate(
                                    R.layout.layout_rv1_item,
                                    parent,
                                    false
                            )
                    );
                case BOTTOM:
                    return new ViewHolder(
                            LayoutInflater.from(parent.getContext()).inflate(
                                    R.layout.layout_item_bottom,
                                    parent,
                                    false
                            )
                    );
                default:
                    throw new IllegalArgumentException("unknown viewType:" + viewType);
            }
        }

        @Override
        public void onBindViewHolderInside(Response data, ViewHolder holder, int position) {
            if (position == 0) {
                holder.tv.setText("Adapter3");
            } else if (position < data.content.size() + 1) {
                holder.tv.setText(data.content.get(position - 1).data);
            } else {
                holder.tv.setText("Adapter3 Bottom");
            }
        }

        @Override
        public Object getRepresentObjectAt(Response data, int position) {
            if (position == 0) {
                return HEADER;
            } else if (position < data.content.size() + 1) {
                return data.content.get(position - 1);
            } else {
                return BOTTOM;
            }
        }
    }

    class Adapter4 implements SubAdapter<Response, ViewHolder> {

        private static final int CONTENT = 0;
        private static final int HEADER = 1;

        // SubAdapter implementation

        @Override
        public Observable<Response> refreshData() {
            return Observable
                    .just(
                            new Response(
                                    200,
                                    Arrays.asList(
                                            new Entity("e"), new Entity("f"), new Entity("g"), new Entity("h")
                                    )
                            )
                    )
                    .delay(1000, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.io());
        }

        @Override
        public int getItemCount(Response data) {
            if (data != null && data.content != null && data.content.size() > 0) {
                return data.content.size() + 1;
            } else {
                return 0;
            }
        }

        @Override
        public int getItemViewTypeInside(Response data, int position) {
            if (position == 0) {
                return HEADER;
            } else {
                return CONTENT;
            }
        }

        @Override
        public ViewHolder onCreateViewHolderInside(Response data, ViewGroup parent, int viewType) {
            switch (viewType) {
                case HEADER:
                    return new ViewHolder(
                            LayoutInflater.from(parent.getContext()).inflate(
                                    R.layout.layout_item_header,
                                    parent,
                                    false
                            )
                    );
                case CONTENT:
                    return new ViewHolder(
                            LayoutInflater.from(parent.getContext()).inflate(
                                    R.layout.layout_rv2_item,
                                    parent,
                                    false
                            )
                    );
                default:
                    throw new IllegalArgumentException("unknown viewType:" + viewType);
            }
        }

        @Override
        public void onBindViewHolderInside(Response data, ViewHolder holder, int position) {
            if (position == 0) {
                holder.tv.setText("Adapter4");
            } else {
                holder.tv.setText(data.content.get(position - 1).data);
            }
        }

        @Override
        public Object getRepresentObjectAt(Response data, int position) {
            if (position == 0) {
                return HEADER;
            } else {
                return data.content.get(position - 1);
            }
        }
    }
}
