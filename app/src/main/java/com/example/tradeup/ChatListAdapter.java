package com.example.tradeup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatVH> {
    private final List<ChatItem> list;
    private final OnChatClickListener listener;

    public ChatListAdapter(List<ChatItem> list, OnChatClickListener l) {
        this.list = list;
        this.listener = l;
    }

    @NonNull
    @Override
    public ChatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        return new ChatVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatVH h, int pos) {
        ChatItem item = list.get(pos);
        h.tvName.setText(item.userName != null ? item.userName : "Chưa rõ tên");
        h.tvLastMessage.setText(item.lastMessage != null ? item.lastMessage : "");

        // Show avatar (dùng trường avatarBase64 hoặc photoBase64, tuỳ bạn)
        String avatar = item.avatarBase64; // Nếu dùng photoBase64, sửa lại thành item.photoBase64
        if (avatar != null && !avatar.isEmpty()) {
            Bitmap bm = decodeBase64(avatar);
            if (bm != null) {
                h.imgAvatar.setImageBitmap(bm);
            } else {
                h.imgAvatar.setImageResource(R.drawable.ic_user);
            }
        } else {
            h.imgAvatar.setImageResource(R.drawable.ic_user);
        }

        // Hiển thị thời gian tin nhắn cuối cùng nếu có
        if (h.tvLastTime != null) {
            if (item.lastTimestamp > 0) {
                h.tvLastTime.setVisibility(View.VISIBLE);
                h.tvLastTime.setText(DateFormat.format("HH:mm", item.lastTimestamp));
            } else {
                h.tvLastTime.setVisibility(View.GONE);
            }
        }

        h.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ChatVH extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvLastMessage, tvLastTime;
        public ChatVH(@NonNull View v) {
            super(v);
            imgAvatar = v.findViewById(R.id.imgAvatar);
            tvName = v.findViewById(R.id.tvUserName);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
            // Nếu trong layout có TextView hiển thị thời gian cuối, lấy luôn:
            tvLastTime = v.findViewById(R.id.tvLastTime); // hoặc null nếu không có trường này trong layout
        }
    }

    public interface OnChatClickListener { void onClick(ChatItem item); }

    // Convert base64 String to Bitmap
    private Bitmap decodeBase64(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}
