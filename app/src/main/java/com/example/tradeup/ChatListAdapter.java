package com.example.tradeup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatVH> {
    private final List<ChatItem> list;
    private final OnChatClickListener listener;

    public ChatListAdapter(List<ChatItem> list, OnChatClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        return new ChatVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatVH h, int position) {
        ChatItem item = list.get(position);

        // Tên người dùng
        h.tvName.setText(item.userName != null ? item.userName : "Chưa rõ tên");

        // Tin nhắn cuối
        h.tvLastMessage.setText(item.lastMessage != null ? item.lastMessage : "");

        // Avatar (base64)
        if (item.avatarBase64 != null && !item.avatarBase64.isEmpty()) {
            Bitmap bm = decodeBase64(item.avatarBase64);
            if (bm != null) {
                h.imgAvatar.setImageBitmap(bm);
            } else {
                h.imgAvatar.setImageResource(R.drawable.ic_user);
            }
        } else {
            h.imgAvatar.setImageResource(R.drawable.ic_user);
        }

        // Thời gian cuối cùng (nếu có)
        if (item.lastTimestamp > 0) {
            h.tvLastTime.setVisibility(View.VISIBLE);
            h.tvLastTime.setText(DateFormat.format("HH:mm", item.lastTimestamp));
        } else {
            h.tvLastTime.setVisibility(View.GONE);
        }

        // Sự kiện click
        h.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnChatClickListener {
        void onClick(ChatItem item);
    }

    static class ChatVH extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvLastMessage, tvLastTime;

        public ChatVH(@NonNull View v) {
            super(v);
            imgAvatar = v.findViewById(R.id.imgAvatar);
            tvName = v.findViewById(R.id.tvUserName);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
            tvLastTime = v.findViewById(R.id.tvLastTime); // TextView thời gian cuối
        }
    }

    private Bitmap decodeBase64(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}
