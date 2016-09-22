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
public class DinnerGroupMemberRecyclerAdapter extends RecyclerView.Adapter<DinnerGroupMemberRecyclerAdapter.ViewHolder> {

    public interface DinnerGroupMemberListener{
        void onDinnerGroupMemberSelected(int position);
    }

    private static final String TAG = DinnerGroupMemberRecyclerAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<DinnerUser> values;
    private WeakReference<DinnerGroupMemberListener> activity;

    public DinnerGroupMemberRecyclerAdapter(Context context, ArrayList<DinnerUser> values, DinnerGroupMemberListener activity) {
        this.context = context;
        this.values = values;
        this.activity = new WeakReference<DinnerGroupMemberListener>(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_dinner_group_member, null);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final DinnerUser currentDinnerGroupMember = values.get(position);
        final int dinnerGroupMemberPosition = position;

        holder.dinnerGroupMemberItemNameTextView.setText(currentDinnerGroupMember.getDisplayName());

        holder.dinnerGroupMemberItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "selectedDinnerGroupMember: " + currentDinnerGroupMember.getDisplayName());
                activity.get().onDinnerGroupMemberSelected(dinnerGroupMemberPosition);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != values ? values.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout dinnerGroupMemberItemLayout;
        TextView dinnerGroupMemberItemNameTextView;

        public ViewHolder(View view) {
            super(view);
            this.dinnerGroupMemberItemLayout = (LinearLayout) view.findViewById(R.id.dinnerGroupMemberItemLayout);
            this.dinnerGroupMemberItemNameTextView = (TextView) view.findViewById(R.id.dinnerGroupMemberItemNameTextView);
        }
    }

}