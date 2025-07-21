package com.example.tradeup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatVH> {
    private final List<ChatItem> list;
    private final OnChatClickListener listener;
    private final String currentUserId;
    private final Context context;

    public ChatListAdapter(Context context, List<ChatItem> list, OnChatClickListener listener, String currentUserId) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.currentUserId = currentUserId;
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

        h.tvName.setText(item.getUserName() != null ? item.getUserName() : "Chưa rõ tên");

        if (item.getAvatarBase64() != null && !item.getAvatarBase64().isEmpty()) {
            Bitmap bm = decodeBase64(item.getAvatarBase64());
            if (bm != null) h.imgAvatar.setImageBitmap(bm);
            else h.imgAvatar.setImageResource(R.drawable.ic_user);
        } else {
            h.imgAvatar.setImageResource(R.drawable.ic_user);
        }

        if (item.isIBlockedThem()) {
            h.tvLastMessage.setText("Đã chặn người dùng");
            h.tvLastMessage.setTextColor(Color.parseColor("#E53935"));
            h.tvLastMessage.setTypeface(null, android.graphics.Typeface.ITALIC);
        } else if (item.isTheyBlockedMe()) {
            h.tvLastMessage.setText("Đã bị người dùng chặn");
            h.tvLastMessage.setTextColor(Color.parseColor("#E53935"));
            h.tvLastMessage.setTypeface(null, android.graphics.Typeface.ITALIC);
        } else {
            String message = item.getLastMessage() != null ? item.getLastMessage() : "";
            String time = item.getLastTimestamp() > 0
                    ? DateFormat.format("HH:mm", item.getLastTimestamp()).toString()
                    : "";

            if (!time.isEmpty()) {
                SpannableString spannable = new SpannableString(message + "  •  " + time);
                int timeStart = message.length() + 4;
                spannable.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#B0B0B0")),
                        timeStart, spannable.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                h.tvLastMessage.setText(spannable);
            } else {
                h.tvLastMessage.setText(message);
            }

            h.tvLastMessage.setTextColor(Color.parseColor("#888888"));
            h.tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        h.itemView.setOnClickListener(v -> {
            if (item.isIBlockedThem()) {
                Toast.makeText(context, "Bạn đã chặn người này", Toast.LENGTH_SHORT).show();
            } else if (item.isTheyBlockedMe()) {
                Toast.makeText(context, "Bạn đã bị người dùng chặn", Toast.LENGTH_SHORT).show();
            } else {
                listener.onClick(item);
            }
        });

        if (item.isTheyBlockedMe()) {
            h.btnMenu.setVisibility(View.GONE);
        } else {
            h.btnMenu.setVisibility(View.VISIBLE);
            h.btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, h.btnMenu);
                popup.getMenuInflater().inflate(R.menu.menu_chat_user, popup.getMenu());

                popup.getMenu().findItem(R.id.menu_block_user).setVisible(!item.isIBlockedThem());
                popup.getMenu().findItem(R.id.menu_unblock_user).setVisible(item.isIBlockedThem());

                popup.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.menu_block_user) {
                        blockUser(item.getUserId());
                        item.setIBlockedThem(true);
                        notifyItemChanged(position);
                        return true;
                    } else if (menuItem.getItemId() == R.id.menu_unblock_user) {
                        unblockUser(item.getUserId());
                        item.setIBlockedThem(false);
                        notifyItemChanged(position);
                        return true;
                    }
                    return false;
                });

                popup.show();
            });
        }
    }

    private void blockUser(String blockedUserId) {
        FirebaseFirestore.getInstance()
                .collection("blocked_users")
                .document(currentUserId)
                .collection("blocked")
                .document(blockedUserId)
                .set(Collections.singletonMap("blockedAt", System.currentTimeMillis()));
    }

    private void unblockUser(String blockedUserId) {
        FirebaseFirestore.getInstance()
                .collection("blocked_users")
                .document(currentUserId)
                .collection("blocked")
                .document(blockedUserId)
                .delete();
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
        TextView tvName, tvLastMessage;
        ImageButton btnMenu;

        public ChatVH(@NonNull View v) {
            super(v);
            imgAvatar = v.findViewById(R.id.imgAvatar);
            tvName = v.findViewById(R.id.tvUserName);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
            btnMenu = v.findViewById(R.id.btnMenu);
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
