package com.example.tradeup;

import java.io.Serializable;

public class ChatItem implements Serializable {
    private String chatId;
    private String userId;
    private String userName;
    private String avatarBase64;
    private String lastMessage;
    private long lastTimestamp;
    private boolean iBlockedThem = false;
    private boolean theyBlockedMe = false;

    public ChatItem() {}

    public ChatItem(String chatId, String userId, String userName,
                    String avatarBase64, String lastMessage, long lastTimestamp) {
        this.chatId = chatId;
        this.userId = userId;
        this.userName = userName;
        this.avatarBase64 = avatarBase64;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    public String getChatId() { return chatId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getAvatarBase64() { return avatarBase64; }
    public String getLastMessage() { return lastMessage; }
    public long getLastTimestamp() { return lastTimestamp; }

    public boolean isIBlockedThem() { return iBlockedThem; }
    public boolean isTheyBlockedMe() { return theyBlockedMe; }

    public void setIBlockedThem(boolean iBlockedThem) { this.iBlockedThem = iBlockedThem; }
    public void setTheyBlockedMe(boolean theyBlockedMe) { this.theyBlockedMe = theyBlockedMe; }

    public void setChatId(String chatId) { this.chatId = chatId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setAvatarBase64(String avatarBase64) { this.avatarBase64 = avatarBase64; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastTimestamp(long lastTimestamp) { this.lastTimestamp = lastTimestamp; }
}