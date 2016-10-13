package com.empire.android.dinnertonight;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lstanzione on 8/11/2016.
 */
public class SuggestionRecyclerAdapter extends RecyclerView.Adapter<SuggestionRecyclerAdapter.ViewHolder> {

    public interface SuggestionListener{
        void onSuggestionSelected(int position);
        void onSuggestionVote(int position);
    }

    private static final String TAG = SuggestionRecyclerAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<Dish> dishList;
    private ArrayList<Suggestion> suggestionList;
    private ArrayList<String> creationUsernameList;
    private WeakReference<SuggestionListener> activity;

    private FirebaseUser user;

    public SuggestionRecyclerAdapter(Context context, ArrayList<Dish> dishList, ArrayList<Suggestion> suggestionList, ArrayList<String> creationUsernameList, SuggestionListener activity) {
        this.context = context;
        this.dishList = dishList;
        this.suggestionList = suggestionList;
        this.creationUsernameList = creationUsernameList;
        this.activity = new WeakReference<SuggestionListener>(activity);

        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_suggestion, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Dish currentDish = dishList.get(position);
        final int suggestionPosition = position;
        String creationUsername = creationUsernameList.get(position);
        Suggestion currentSuggestion = suggestionList.get(position);

        holder.suggestionItemNameTextView.setText(currentDish.getName());
        holder.suggestionItemUsernameTextView.setText("By: " + creationUsername);
        holder.suggestionItemVotesTextView.setText("Votes: " + currentSuggestion.getVotes());

        String timeAgo = DateUtils.getRelativeTimeSpanString(currentSuggestion.getCreationTimestamp(), new Date().getTime(), 10).toString();
        holder.suggestionItemTimeAgoTextView.setText(timeAgo);

        /*holder.suggestionItemCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "selectedSuggestion: " + currentDish.getName());
                activity.get().onSuggestionSelected(suggestionPosition);
            }
        });*/

        ArrayList<String> voteUsersList = currentSuggestion.getVoteUsers();

        holder.suggestionItemUpvoteButton.setIconSizeDp(25);

        if(voteUsersList.contains(user.getUid())){
            holder.suggestionItemUpvoteButton.setLiked(true);
        }
        else{
            holder.suggestionItemUpvoteButton.setLiked(false);
        }

        holder.suggestionItemUpvoteButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                activity.get().onSuggestionVote(suggestionPosition);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                activity.get().onSuggestionVote(suggestionPosition);
            }
        });

        /*holder.suggestionItemUpvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.get().onSuggestionVote(suggestionPosition);
            }
        });*/

    }

    @Override
    public int getItemCount() {
        return (null != dishList ? dishList.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView suggestionItemCardView;
        //LinearLayout suggestionItemLayout;
        TextView suggestionItemNameTextView;
        TextView suggestionItemUsernameTextView;
        TextView suggestionItemVotesTextView;
        LikeButton suggestionItemUpvoteButton;
        TextView suggestionItemTimeAgoTextView;

        public ViewHolder(View view) {
            super(view);
            this.suggestionItemCardView = (CardView) view.findViewById(R.id.suggestionItemCardView);
            //this.suggestionItemLayout = (LinearLayout) view.findViewById(R.id.suggestionItemLayout);
            this.suggestionItemNameTextView = (TextView) view.findViewById(R.id.suggestionItemNameTextView);
            this.suggestionItemUsernameTextView = (TextView) view.findViewById(R.id.suggestionItemUsernameTextView);
            this.suggestionItemVotesTextView = (TextView) view.findViewById(R.id.suggestionItemVotesTextView);
            this.suggestionItemUpvoteButton = (LikeButton) view.findViewById(R.id.suggestionItemUpvoteButton);
            this.suggestionItemTimeAgoTextView = (TextView) view.findViewById(R.id.suggestionItemTimeAgoTextView);
        }
    }

}