package com.empire.android.dinnertonight;

import android.content.Context;
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
public class DinnerGroupRecyclerAdapter extends RecyclerView.Adapter<DinnerGroupRecyclerAdapter.ViewHolder> {

    public interface DinnerGroupListener{
        void onDinnerGroupSelected(int position);
    }

    private static final String TAG = DinnerGroupRecyclerAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<DinnerGroup> values;
    private WeakReference<DinnerGroupListener> activity;

    public DinnerGroupRecyclerAdapter(Context context, ArrayList<DinnerGroup> values, DinnerGroupListener activity) {
        this.context = context;
        this.values = values;
        this.activity = new WeakReference<DinnerGroupListener>(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_dinner_group, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final DinnerGroup currentDinnerGroup = values.get(position);
        final int dinnerGroupPosition = position;

        holder.dinnerGroupItemNameTextView.setText(currentDinnerGroup.getName());

        holder.dinnerGroupItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "selectedDinnerGroup: " + currentDinnerGroup.getName());
                activity.get().onDinnerGroupSelected(dinnerGroupPosition);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != values ? values.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout dinnerGroupItemLayout;
        TextView dinnerGroupItemNameTextView;

        public ViewHolder(View view) {
            super(view);
            this.dinnerGroupItemLayout = (LinearLayout) view.findViewById(R.id.dinnerGroupItemLayout);
            this.dinnerGroupItemNameTextView = (TextView) view.findViewById(R.id.dinnerGroupItemNameTextView);
        }
    }

}