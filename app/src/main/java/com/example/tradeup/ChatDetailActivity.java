package com.example.tradeup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class ChatDetailActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1699;

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend, btnSendImage;
    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private String chatId, currentUserId, otherUserId, otherUserName, avatarBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSendImage = findViewById(R.id.btnSendImage); // QUAN TRỌNG: Phải khởi tạo!

        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");
        avatarBase64 = getIntent().getStringExtra("avatarBase64"); // PHẢI là photoBase64 lấy từ Firestore của người còn lại!
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
        // Nếu gửi ảnh thì text có thể để trống
        if (text.isEmpty() && imageBase64 == null) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", currentUserId);
        msg.put("receiverId", otherUserId);
        msg.put("text", !text.isEmpty() ? text : null);
        msg.put("timestamp", System.currentTimeMillis());
        msg.put("image", imageBase64);

        // Lưu vào messages subcollection
        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .add(msg);

        // Cập nhật lastMessage cho chat
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
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh gửi chat"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                String imgBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                sendMessage(imgBase64);
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi gửi ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Message model class (giống như MessageAdapter dùng)
    public static class Message {
        public String senderId, receiverId, text, image;
        public long timestamp;
        public Message() {}
    }
}
