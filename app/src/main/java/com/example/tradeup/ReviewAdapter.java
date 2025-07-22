package com.example.tradeup;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewVH> {
    private final List<Review> reviews;
    private final String currentUserId;
    private final String productId;
    private final String sellerId;

    public ReviewAdapter(List<Review> reviews, String productId, String sellerId) {
        this.reviews = reviews;
        this.productId = productId;
        this.sellerId = sellerId;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ReviewVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewVH h, int position) {
        Review r = reviews.get(position);

        h.tvName.setText(r.getUserName() != null ? r.getUserName() : "Người dùng ẩn danh");
        h.ratingBar.setRating(r.getRating());
        h.tvComment.setText(r.getComment() != null ? r.getComment() : "");

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        h.tvTime.setText(sdf.format(new Date(r.getTimestamp())));

        if (r.getUserAvatar() != null && !r.getUserAvatar().isEmpty()) {
            Bitmap bm = decodeBase64(r.getUserAvatar());
            if (bm != null) {
                h.imgAvatar.setImageBitmap(bm);
            } else {
                h.imgAvatar.setImageResource(R.drawable.ic_user);
            }
        } else {
            h.imgAvatar.setImageResource(R.drawable.ic_user);
        }

        // Hiển thị nút X nếu là người đăng
        if (currentUserId.equals(r.getUserId())) {
            h.btnDelete.setVisibility(View.VISIBLE);
            h.btnDelete.setOnClickListener(v -> confirmDelete(h.itemView.getContext(), position, r));
        } else {
            h.btnDelete.setVisibility(View.GONE);
        }
    }

    private void confirmDelete(Context context, int position, Review review) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá đánh giá này?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteReviewFromFirestore(review, position))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteReviewFromFirestore(Review review, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Xoá đánh giá sản phẩm
        db.collection("products")
                .document(productId)
                .collection("reviews")
                .whereEqualTo("userId", review.getUserId())
                .whereEqualTo("timestamp", review.getTimestamp())
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().delete();
                    }

                    // Cập nhật UI sau khi xoá
                    reviews.remove(position);
                    notifyItemRemoved(position);
                });

        // 2. Xoá đánh giá người bán (nếu có)
        db.collection("users")
                .document(sellerId)
                .collection("ratings")
                .whereEqualTo("fromUserId", review.getUserId())
                .whereEqualTo("timestamp", review.getTimestamp())
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().delete();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewVH extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvComment, tvTime;
        RatingBar ratingBar;
        ImageButton btnDelete;

        public ReviewVH(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvTime = itemView.findViewById(R.id.tvTime);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnDelete = itemView.findViewById(R.id.btnDelete);
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
