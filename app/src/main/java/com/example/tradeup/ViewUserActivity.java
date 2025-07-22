package com.example.tradeup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ViewUserActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvName, tvEmail, tvPhone, tvHomeTown, tvDob;
    private RecyclerView rvUserProducts;

    private List<Product> productList = new ArrayList<>();
    private HomeProductAdapter productAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);

        // Ánh xạ view
        imgAvatar = findViewById(R.id.imgUserAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvHomeTown = findViewById(R.id.tvHomeTown);
        tvDob = findViewById(R.id.tvDob);
        rvUserProducts = findViewById(R.id.rvUserProducts);

        // Cấu hình RecyclerView
        rvUserProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new HomeProductAdapter(this, productList, null); // listener = null để không mở chi tiết
        rvUserProducts.setAdapter(productAdapter);

        // Lấy userId được truyền từ ProductDetailFragment
        String userId = getIntent().getStringExtra("userId");
        if (userId != null && !userId.isEmpty()) {
            loadUserInfo(userId);
            loadUserProducts(userId);
        }
    }

    private void loadUserInfo(String userId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvName.setText(getOrDefault(doc.getString("name")));
                        tvEmail.setText(getOrDefault(doc.getString("email")));
                        tvPhone.setText(getOrDefault(doc.getString("phone")));
                        tvHomeTown.setText(getOrDefault(doc.getString("homeTown")));
                        tvDob.setText(getOrDefault(doc.getString("dob")));

                        String avatarBase64 = doc.getString("photoBase64");
                        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
                            try {
                                byte[] bytes = Base64.decode(avatarBase64, Base64.DEFAULT);
                                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                imgAvatar.setImageBitmap(bm);
                            } catch (Exception e) {
                                imgAvatar.setImageResource(R.drawable.ic_user);
                            }
                        } else {
                            imgAvatar.setImageResource(R.drawable.ic_user);
                        }
                    }
                });
    }

    private void loadUserProducts(String userId) {
        FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    productList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) productList.add(p);
                    }
                    productAdapter.notifyDataSetChanged();
                });
    }

    private String getOrDefault(String value) {
        return (value == null || value.trim().isEmpty()) ? "(Không có dữ liệu)" : value;
    }
}
