package com.skillsearch.skillsearch;

/**
 * Created by COld on 4/10/2016.
 */

public class Contact {

    private String userId;
    private String facebookId;
    private String nameAndSkill;
    private String lastMessage;
    private String unread;

    public Contact (String userId, String facebookId, String nameAndSkill, String lastMessage, String unread) {
        this.userId = userId;
        this.facebookId = facebookId;
        this.nameAndSkill = nameAndSkill;
        this.lastMessage = lastMessage;
        this.unread = unread;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getFacebookId() {
        return this.facebookId;
    }

    public String getNameAndSkill() {
        return this.nameAndSkill;
    }

    public String getLastMessage() {
        return this.lastMessage;
    }

    public String getUnread() {
        return this.unread;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public void setNameAndSkill(String nameAndSkill) {
        this.nameAndSkill = nameAndSkill;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setUnread(String unread) {
        this.unread = unread;
    }
}
