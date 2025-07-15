package com.example.tradeup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatFragment extends Fragment {
    private RecyclerView rvChatList;
    private TextView tvEmptyChat;
    private ChatListAdapter adapter;
    private final List<ChatItem> chatList = new ArrayList<>();
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        rvChatList = v.findViewById(R.id.rvChatList);
        tvEmptyChat = v.findViewById(R.id.tvEmptyChat);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new ChatListAdapter(chatList, this::openChat);
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatList.setAdapter(adapter);

        loadChatList();
        return v;
    }

    private void loadChatList() {
        FirebaseFirestore.getInstance().collection("chats")
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    chatList.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            String chatId = doc.getId();
                            if (chatId.contains(currentUserId)) {
                                String lastMsg = doc.getString("lastMessage");
                                long lastTimestamp = doc.contains("lastTimestamp") ? doc.getLong("lastTimestamp") : 0;
                                String otherId = getOtherUserIdFromChatId(chatId, currentUserId);

                                FirebaseFirestore.getInstance().collection("users")
                                        .document(otherId)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            String otherName = userDoc.getString("name");
                                            String photoBase64 = userDoc.getString("photoBase64");

                                            if (otherName == null) otherName = "Người dùng";
                                            if (photoBase64 == null) photoBase64 = "";

                                            chatList.add(new ChatItem(chatId, otherId, otherName, photoBase64, lastMsg, lastTimestamp));
                                            adapter.notifyDataSetChanged();
                                            tvEmptyChat.setVisibility(chatList.isEmpty() ? View.VISIBLE : View.GONE);
                                        });
                            }
                        }
                    }
                });
    }

    private String getOtherUserIdFromChatId(String chatId, String myId) {
        String[] ids = chatId.split("_");
        return ids[0].equals(myId) ? ids[1] : ids[0];
    }

    private void openChat(ChatItem item) {
        if (item == null || item.chatId == null || item.otherUserId == null) {
            Toast.makeText(getContext(), "Không thể mở đoạn chat!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), ChatDetailActivity.class);
        intent.putExtra("chatId", item.chatId);
        intent.putExtra("otherUserId", item.otherUserId);
        intent.putExtra("otherUserName", item.userName);
        intent.putExtra("avatarBase64", item.photoBase64); // truyền avatar để hiển thị trong ChatDetail
        startActivity(intent);
    }

    // ----------------- Adapter cho Chat List -----------------
    static class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatVH> {
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
            h.tvName.setText(item.userName != null ? item.userName : "Người dùng");
            h.tvLastMessage.setText(item.lastMessage != null ? item.lastMessage : "");

            if (item.photoBase64 != null && !item.photoBase64.isEmpty()) {
                Bitmap bm = decodeBase64(item.photoBase64);
                if (bm != null) {
                    h.imgAvatar.setImageBitmap(bm);
                } else {
                    h.imgAvatar.setImageResource(R.drawable.ic_user);
                }
            } else {
                h.imgAvatar.setImageResource(R.drawable.ic_user);
            }

            h.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ChatVH extends RecyclerView.ViewHolder {
            ImageView imgAvatar;
            TextView tvName, tvLastMessage;

            public ChatVH(@NonNull View v) {
                super(v);
                imgAvatar = v.findViewById(R.id.imgAvatar);
                tvName = v.findViewById(R.id.tvUserName);
                tvLastMessage = v.findViewById(R.id.tvLastMessage);
            }
        }

        public interface OnChatClickListener {
            void onClick(ChatItem item);
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

    // Model dữ liệu Chat List
    static class ChatItem {
        String chatId, otherUserId, userName, photoBase64, lastMessage;
        long lastTimestamp;

        public ChatItem(String chatId, String otherUserId, String userName, String photoBase64, String lastMessage, long lastTimestamp) {
            this.chatId = chatId;
            this.otherUserId = otherUserId;
            this.userName = userName;
            this.photoBase64 = photoBase64;
            this.lastMessage = lastMessage;
            this.lastTimestamp = lastTimestamp;
        }
    }
}
