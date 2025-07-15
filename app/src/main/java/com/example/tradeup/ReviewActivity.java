package com.example.tradeup;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private RatingBar ratingBarProduct, ratingBarSeller;
    private EditText edtCommentProduct, edtCommentSeller;
    private Button btnSubmit;

    private String productId, sellerId, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Ánh xạ view
        ratingBarProduct = findViewById(R.id.ratingBarProduct);
        ratingBarSeller = findViewById(R.id.ratingBarSeller);
        edtCommentProduct = findViewById(R.id.edtCommentProduct);
        edtCommentSeller = findViewById(R.id.edtCommentSeller);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Nhận dữ liệu từ Intent
        productId = getIntent().getStringExtra("productId");
        sellerId = getIntent().getStringExtra("sellerId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Ràng buộc không cho tự đánh giá chính mình
        if (currentUserId.equals(sellerId)) {
            Toast.makeText(this, "Bạn không thể tự đánh giá chính mình!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        float ratingProduct = ratingBarProduct.getRating();
        float ratingSeller = ratingBarSeller.getRating();
        String commentProduct = edtCommentProduct.getText().toString().trim();
        String commentSeller = edtCommentSeller.getText().toString().trim();

        if (ratingProduct == 0 && ratingSeller == 0) {
            Toast.makeText(this, "Bạn chưa đánh giá gì cả!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Gửi đánh giá sản phẩm (chỉ nếu người đánh giá KHÔNG phải là chủ sản phẩm)
        if (ratingProduct > 0 && !currentUserId.equals(sellerId)) {
            Map<String, Object> productReview = new HashMap<>();
            productReview.put("userId", currentUserId);
            productReview.put("rating", ratingProduct);
            productReview.put("comment", commentProduct);
            productReview.put("timestamp", System.currentTimeMillis());

            db.collection("products")
                    .document(productId)
                    .collection("reviews")
                    .add(productReview);
        }

        // Gửi đánh giá người bán
        if (ratingSeller > 0 && !currentUserId.equals(sellerId)) {
            Map<String, Object> sellerReview = new HashMap<>();
            sellerReview.put("fromUserId", currentUserId);
            sellerReview.put("rating", ratingSeller);
            sellerReview.put("comment", commentSeller);
            sellerReview.put("timestamp", System.currentTimeMillis());

            db.collection("users")
                    .document(sellerId)
                    .collection("ratings")
                    .add(sellerReview);
        }

        Toast.makeText(this, "Đánh giá đã được gửi!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
