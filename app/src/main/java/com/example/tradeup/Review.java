package com.example.tradeup;

import java.io.Serializable;

/**
 * Model cho 1 đánh giá (review) của người dùng.
 * Dùng cho đánh giá sản phẩm hoặc người bán.
 */
public class Review implements Serializable {
    private String userId;       // ID của người đánh giá
    private String userName;     // Tên người đánh giá
    private String userAvatar;   // Ảnh đại diện (base64)
    private String comment;      // Nội dung bình luận
    private float rating;        // Số sao đánh giá
    private long timestamp;      // Thời gian đánh giá (millis)

    // Bắt buộc có constructor rỗng cho Firestore
    public Review() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName != null ? userName : "";
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar != null ? userAvatar : "";
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getComment() {
        return comment != null ? comment : "";
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
