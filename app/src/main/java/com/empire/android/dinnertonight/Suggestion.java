package com.empire.android.dinnertonight;

import java.util.ArrayList;

/**
 * Created by lstanzione on 9/22/2016.
 */
public class Suggestion {

    private String id;
    private String day;
    private int votes;
    private ArrayList<String> voteUsers;
    private String dishId;
    private String creationUserId;
    private long creationTimestamp;
    private String modificationUserId;
    private long modificationTimestamp;
    private boolean active;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void addVote(){
        this.votes++;
    }

    public void removeVote(){
        this.votes--;
    }

    public ArrayList<String> getVoteUsers() {

        if(this.voteUsers == null){
            this.voteUsers = new ArrayList<String>();
        }

        return voteUsers;
    }

    public void setVoteUsers(ArrayList<String> voteUsers) {
        this.voteUsers = voteUsers;
    }

    public void addVoteUser(String userId){

        if(this.voteUsers == null){
            this.voteUsers = new ArrayList<String>();
        }

        this.voteUsers.add(userId);
    }

    public void removeVoteUser(String userId){
        this.voteUsers.remove(userId);
    }

    public String getDishId() {
        return dishId;
    }

    public void setDishId(String dishId) {
        this.dishId = dishId;
    }

    public String getCreationUserId() {
        return creationUserId;
    }

    public void setCreationUserId(String creationUserId) {
        this.creationUserId = creationUserId;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getModificationUserId() {
        return modificationUserId;
    }

    public void setModificationUserId(String modificationUserId) {
        this.modificationUserId = modificationUserId;
    }

    public long getModificationTimestamp() {
        return modificationTimestamp;
    }

    public void setModificationTimestamp(long modificationTimestamp) {
        this.modificationTimestamp = modificationTimestamp;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
