package com.example.tradeup;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

public class ChatDetailActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1699;

    private RecyclerView rvMessages;
    private EmojiEditText etMessage;
    private ImageButton btnSend, btnSendImage, btnEmoji;
    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private String chatId, currentUserId, otherUserId, otherUserName, avatarBase64;
    private EmojiPopup emojiPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSendImage = findViewById(R.id.btnSendImage);
        btnEmoji = findViewById(R.id.btnEmoji);

        // Init emoji popup
        emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(android.R.id.content)).build(etMessage);

        btnEmoji.setOnClickListener(v -> emojiPopup.toggle());

        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");
        avatarBase64 = getIntent().getStringExtra("avatarBase64");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setTitle("Chat với " + (otherUserName != null ? otherUserName : "Người dùng"));

        adapter = new MessageAdapter(messageList, currentUserId, avatarBase64);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        loadMessages();

        btnSend.setOnClickListener(v -> sendMessage(null));
        btnSendImage.setOnClickListener(v -> pickImage());
    }

    private void loadMessages() {
        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    messageList.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Message msg = doc.toObject(Message.class);
                            if (msg != null) messageList.add(msg);
                        }
                        adapter.notifyDataSetChanged();
                        rvMessages.scrollToPosition(Math.max(messageList.size() - 1, 0));
                    }
                });
    }

    private void sendMessage(@Nullable String imageBase64) {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty() && imageBase64 == null) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", currentUserId);
        msg.put("receiverId", otherUserId);
        msg.put("text", !text.isEmpty() ? text : null);
        msg.put("timestamp", System.currentTimeMillis());
        msg.put("image", imageBase64);

        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .add(msg);

        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId)
                .set(new HashMap<String, Object>() {{
                    put("lastMessage", (text != null && !text.isEmpty()) ? text : "[Đã gửi ảnh]");
                    put("lastTimestamp", System.currentTimeMillis());
                }}, SetOptions.merge());

        etMessage.setText("");
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh gửi chat"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            List<Uri> imageUris = new ArrayList<>();
            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    imageUris.add(clipData.getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }

            for (Uri uri : imageUris) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    String imgBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                    sendMessage(imgBase64);
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi gửi ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static class Message {
        public String senderId, receiverId, text, image;
        public long timestamp;
        public Message() {}
    }
}
