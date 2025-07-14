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

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatDetailActivity.Message> messages;
    private final String currentUserId;
    private final String avatarBase64; // Avatar của người kia (photoBase64)

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    public MessageAdapter(List<ChatDetailActivity.Message> messages, String currentUserId, String avatarBase64) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.avatarBase64 = avatarBase64;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).senderId.equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentVH(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedVH(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatDetailActivity.Message msg = messages.get(position);

        if (getItemViewType(position) == TYPE_SENT) {
            SentVH vh = (SentVH) holder;
            bindMessage(vh.tvMsg, vh.imgMsg, vh.tvTime, msg);
        } else {
            ReceivedVH vh = (ReceivedVH) holder;
            bindMessage(vh.tvMsg, vh.imgMsg, vh.tvTime, msg);

            // Hiển thị avatar (chỉ bên nhận)
            if (vh.imgAvatar != null) {
                if (avatarBase64 != null && !avatarBase64.isEmpty()) {
                    Bitmap bm = base64ToBitmap(avatarBase64);
                    if (bm != null) {
                        vh.imgAvatar.setImageBitmap(bm);
                    } else {
                        vh.imgAvatar.setImageResource(R.drawable.ic_user);
                    }
                } else {
                    vh.imgAvatar.setImageResource(R.drawable.ic_user);
                }
            }
        }
    }

    // Hàm chung để gán text/ảnh/gio
    private void bindMessage(TextView tvMsg, ImageView imgMsg, TextView tvTime, ChatDetailActivity.Message msg) {
        if (msg.text != null && !msg.text.isEmpty()) {
            tvMsg.setVisibility(View.VISIBLE);
            tvMsg.setText(msg.text);
        } else {
            tvMsg.setVisibility(View.GONE);
        }
        if (msg.image != null && !msg.image.isEmpty()) {
            imgMsg.setVisibility(View.VISIBLE);
            Bitmap bitmap = base64ToBitmap(msg.image);
            imgMsg.setImageBitmap(bitmap);
        } else {
            imgMsg.setVisibility(View.GONE);
        }
        tvTime.setText(DateFormat.format("HH:mm", msg.timestamp));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder cho tin nhắn gửi
    static class SentVH extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        ImageView imgMsg;
        SentVH(@NonNull View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMsg);
            imgMsg = v.findViewById(R.id.imgMsg);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }

    // ViewHolder cho tin nhắn nhận (có avatar)
    static class ReceivedVH extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        ImageView imgMsg, imgAvatar;
        ReceivedVH(@NonNull View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMsg);
            imgMsg = v.findViewById(R.id.imgMsg);
            tvTime = v.findViewById(R.id.tvTime);
            imgAvatar = v.findViewById(R.id.imgAvatar);
        }
    }

    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] bytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}
