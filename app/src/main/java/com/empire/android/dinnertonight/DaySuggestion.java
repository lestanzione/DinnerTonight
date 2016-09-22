package com.empire.android.dinnertonight;

/**
 * Created by lstanzione on 9/22/2016.
 */
public class DaySuggestion {

    private String id;
    private String day;
    private String dishId;
    private String creationUserId;
    private String creationTimestamp;
    private String modificationUserId;
    private String modificationTimestamp;
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

    public String getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(String creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getModificationUserId() {
        return modificationUserId;
    }

    public void setModificationUserId(String modificationUserId) {
        this.modificationUserId = modificationUserId;
    }

    public String getModificationTimestamp() {
        return modificationTimestamp;
    }

    public void setModificationTimestamp(String modificationTimestamp) {
        this.modificationTimestamp = modificationTimestamp;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
