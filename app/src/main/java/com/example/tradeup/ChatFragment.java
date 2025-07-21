package com.example.tradeup;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

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
        adapter = new ChatListAdapter(getContext(), chatList, this::openChat, currentUserId);
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatList.setAdapter(adapter);

        loadChatList();
        return v;
    }

    private void loadChatList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("blocked_users")
                .document(currentUserId)
                .collection("blocked")
                .get()
                .addOnSuccessListener(blockedSnapshots -> {
                    Set<String> blockedUserIds = new HashSet<>();
                    for (DocumentSnapshot doc : blockedSnapshots) {
                        blockedUserIds.add(doc.getId());
                    }

                    db.collection("chats")
                            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                            .addSnapshotListener((snapshots, e) -> {
                                chatList.clear();
                                if (snapshots != null) {
                                    for (DocumentSnapshot doc : snapshots) {
                                        String chatId = doc.getId();
                                        if (!chatId.contains(currentUserId)) continue;

                                        String lastMsg = doc.getString("lastMessage");
                                        long lastTimestamp = doc.contains("lastTimestamp") ? doc.getLong("lastTimestamp") : 0;
                                        String otherUserId = getOtherUserIdFromChatId(chatId, currentUserId);

                                        boolean iBlockedThem = blockedUserIds.contains(otherUserId);

                                        db.collection("users")
                                                .document(otherUserId)
                                                .get()
                                                .addOnSuccessListener(userDoc -> {
                                                    String name = userDoc.getString("name");
                                                    String photo = userDoc.getString("photoBase64");
                                                    if (name == null) name = "Người dùng";
                                                    if (photo == null) photo = "";

                                                    ChatItem item = new ChatItem(chatId, otherUserId, name, photo, lastMsg, lastTimestamp);
                                                    item.setIBlockedThem(iBlockedThem);

                                                    db.collection("blocked_users")
                                                            .document(otherUserId)
                                                            .collection("blocked")
                                                            .document(currentUserId)
                                                            .get()
                                                            .addOnSuccessListener(blockedMe -> {
                                                                item.setTheyBlockedMe(blockedMe.exists());
                                                                chatList.add(item);
                                                                adapter.notifyDataSetChanged();
                                                                tvEmptyChat.setVisibility(chatList.isEmpty() ? View.VISIBLE : View.GONE);
                                                            });
                                                });
                                    }
                                }
                            });
                });
    }

    private String getOtherUserIdFromChatId(String chatId, String myId) {
        String[] ids = chatId.split("_");
        return ids[0].equals(myId) ? ids[1] : ids[0];
    }

    private void openChat(ChatItem item) {
        if (item == null || item.getChatId() == null || item.getUserId() == null) {
            Toast.makeText(getContext(), "Không thể mở đoạn chat!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (item.isIBlockedThem()) {
            Toast.makeText(getContext(), "Bạn đã chặn người này", Toast.LENGTH_SHORT).show();
            return;
        }

        if (item.isTheyBlockedMe()) {
            Toast.makeText(getContext(), "Bạn đã bị người dùng chặn", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), ChatDetailActivity.class);
        intent.putExtra("chatId", item.getChatId());
        intent.putExtra("otherUserId", item.getUserId());
        intent.putExtra("otherUserName", item.getUserName());
        intent.putExtra("avatarBase64", item.getAvatarBase64());
        startActivity(intent);
    }
}
