package prototypez.github.io.complexadapter.demo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
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
import io.reactivex.Scheduler;
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

        ComplexAdapter adapter = new ComplexAdapter(
                Arrays.asList(
                        new Adapter1(Arrays.asList(new Entity("a"), new Entity("b"))),
                        new Adapter2(Arrays.asList(new Entity("c"), new Entity("d")))
                ),
                Arrays.asList(1, 2)
        );

        mBinding.rv1.setAdapter(adapter);
        mBinding.rv1.setLayoutManager(new LinearLayoutManager(this));

        mBinding.refresh.setOnClickListener(v -> adapter.refresh());
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }
    }

    class Entity implements AdapterItem {

        String data;

        public Entity(String data) {
            this.data = data;
        }

        @Override
        public boolean isItemTheSameTo(AdapterItem adapterItem) {
            if (adapterItem instanceof Entity) {
                return ((Entity) adapterItem).data.equals(data);
            } else {
                return false;
            }
        }

        @Override
        public boolean isContentTheSameTo(AdapterItem adapterItem) {
            if (adapterItem instanceof Entity) {
                return ((Entity) adapterItem).data.equals(data);
            } else {
                return false;
            }
        }
    }

    class Adapter1 extends RecyclerView.Adapter<ViewHolder> implements SubAdapter<Entity, ViewHolder> {

        List<Entity> mEntities;

        Adapter1(List<Entity> entities) {
            mEntities = entities;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return onCreateViewHolderInside(mEntities, parent, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            onBindViewHolderInside(mEntities, holder, position);
        }

        @Override
        public int getItemCount() {
            return mEntities.size();
        }

        // SubAdapter implementation

        @Override
        public Observable<List<Entity>> refreshData() {
            return Observable
                    .just(Arrays.asList(new Entity("1"), new Entity("2"), new Entity("3")))
                    .delay(2000, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.io());
        }

        @Override
        public int getItemViewTypeInside(List<Entity> data, int position) {
            return 0;
        }

        @Override
        public ViewHolder onCreateViewHolderInside(List<Entity> data, ViewGroup parent, int viewType) {
            return new ViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.layout_rv1_item,
                            parent,
                            false
                    )
            );
        }

        @Override
        public void onBindViewHolderInside(List<Entity> data, ViewHolder holder, int position) {
            holder.tv.setText(data.get(position).data);
        }
    }


    class Adapter2 extends RecyclerView.Adapter<ViewHolder> implements SubAdapter<Entity, ViewHolder> {

        List<Entity> mEntities;

        Adapter2(List<Entity> entities) {
            mEntities = entities;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return onCreateViewHolderInside(mEntities, parent, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            onBindViewHolderInside(mEntities, holder, position);
        }

        @Override
        public int getItemCount() {
            return mEntities.size();
        }

        // SubAdapter implementation

        @Override
        public Observable<List<Entity>> refreshData() {
            return Observable
                    .just(Arrays.asList(new Entity("1"), new Entity("2"), new Entity("3"), new Entity("4")))
                    .delay(1000, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.io());
        }

        @Override
        public int getItemViewTypeInside(List<Entity> data, int position) {
            return 0;
        }

        @Override
        public ViewHolder onCreateViewHolderInside(List<Entity> data, ViewGroup parent, int viewType) {
            return new ViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.layout_rv2_item,
                            parent,
                            false
                    )
            );
        }

        @Override
        public void onBindViewHolderInside(List<Entity> data, ViewHolder holder, int position) {
            holder.tv.setText(data.get(position).data);
        }
    }
}
