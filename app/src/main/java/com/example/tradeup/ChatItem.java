package com.example.tradeup;

import java.io.Serializable;

public class ChatItem implements Serializable {
    public String chatId;
    public String otherUserId;
    public String userName;
    public String avatarBase64;
    public String lastMessage;
    public long lastTimestamp;

    // Constructor đầy đủ các trường
    public ChatItem(String chatId, String otherUserId, String userName, String avatarBase64, String lastMessage, long lastTimestamp) {
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.userName = userName;
        this.avatarBase64 = avatarBase64;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    // Constructor nếu cần (dùng khi không có avatar)
    public ChatItem(String chatId, String otherUserId, String userName, String lastMessage, long lastTimestamp) {
        this(chatId, otherUserId, userName, "", lastMessage, lastTimestamp);
    }

    // Nếu muốn có getter/setter thì thêm vào dưới đây, còn không thì dùng public như trên là được.
}
