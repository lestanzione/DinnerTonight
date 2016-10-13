package com.empire.android.dinnertonight;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by lstanzione on 8/11/2016.
 */
public class DishRecyclerAdapter extends RecyclerView.Adapter<DishRecyclerAdapter.ViewHolder> {

    public interface DishListener{
        void onDishSelected(int position);
    }

    private static final String TAG = DishRecyclerAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<Dish> values;
    private WeakReference<DishListener> activity;

    public DishRecyclerAdapter(Context context, ArrayList<Dish> values, DishListener activity) {
        this.context = context;
        this.values = values;
        this.activity = new WeakReference<DishListener>(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_dish, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Dish currentDish = values.get(position);
        final int dishPosition = position;

        holder.dishItemNameTextView.setText(currentDish.getName());

        holder.dishItemCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "selectedSuggestion: " + currentDish.getName());
                activity.get().onDishSelected(dishPosition);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != values ? values.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView dishItemCardView;
        LinearLayout dishItemLayout;
        TextView dishItemNameTextView;

        public ViewHolder(View view) {
            super(view);
            this.dishItemCardView = (CardView) view.findViewById(R.id.dishItemCardView);
            this.dishItemLayout = (LinearLayout) view.findViewById(R.id.dishItemLayout);
            this.dishItemNameTextView = (TextView) view.findViewById(R.id.dishItemNameTextView);
        }
    }

}