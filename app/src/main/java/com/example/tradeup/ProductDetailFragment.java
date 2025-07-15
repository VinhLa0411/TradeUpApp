package com.example.tradeup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.NumberFormat;
import java.util.*;

public class ProductDetailFragment extends Fragment {
    private ViewPager2 viewPagerImages;
    private LinearLayout layoutDots;
    private TextView tvName, tvPrice, tvBrand, tvYear, tvQuantity, tvOwner, tvDescription;
    private TextView tvProductReviews, tvSellerReviews;
    private RatingBar productRatingBar, sellerRatingBar;
    private Button btnContact, btnPay, btnReview;

    private Product product;
    private List<String> imageBase64s = new ArrayList<>();

    public ProductDetailFragment() {}

    public static ProductDetailFragment newInstance(Product product) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable("product");
            if (product != null && product.getImages() != null) {
                imageBase64s = product.getImages();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        viewPagerImages = view.findViewById(R.id.viewPagerImages);
        layoutDots = view.findViewById(R.id.layoutDots);
        tvName = view.findViewById(R.id.tvName);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvBrand = view.findViewById(R.id.tvBrand);
        tvYear = view.findViewById(R.id.tvYear);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvOwner = view.findViewById(R.id.tvOwner);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvProductReviews = view.findViewById(R.id.tvProductReviews);
        tvSellerReviews = view.findViewById(R.id.tvSellerReviews);
        productRatingBar = view.findViewById(R.id.productRatingBar);
        sellerRatingBar = view.findViewById(R.id.sellerRatingBar);
        btnContact = view.findViewById(R.id.btnContact);
        btnPay = view.findViewById(R.id.btnPay);
        btnReview = view.findViewById(R.id.btnReview);

        if (product != null) {
            showProductInfo(view);
            loadProductRating();
            loadSellerRating();
        }

        return view;
    }

    private void showProductInfo(View view) {
        viewPagerImages.setAdapter(new ImageSliderAdapter(imageBase64s));
        setupDots(imageBase64s.size());
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectDot(position);
            }
        });

        tvName.setText(product.getName());
        tvBrand.setText("Hãng: " + product.getBrand());
        tvYear.setText("Năm sản xuất: " + product.getYear());
        tvQuantity.setText("Số lượng: " + product.getQuantity());

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvPrice.setText("Giá: " + nf.format(product.getPrice()) + " VNĐ");

        tvOwner.setText(!TextUtils.isEmpty(product.getOwnerName()) ?
                "Người đăng: " + product.getOwnerName() : "Người đăng: (không rõ)");

        tvDescription.setText(!TextUtils.isEmpty(product.getDescription()) ?
                product.getDescription() : "(Không có mô tả)");

        btnContact.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String otherUserId = product.getOwnerId();

            if (otherUserId != null && !otherUserId.equals(currentUserId)) {
                String chatId = (currentUserId.compareTo(otherUserId) < 0)
                        ? currentUserId + "_" + otherUserId : otherUserId + "_" + currentUserId;

                FirebaseFirestore.getInstance().collection("users")
                        .document(otherUserId)
                        .get()
                        .addOnSuccessListener(userDoc -> {
                            String ownerAvatar = userDoc.getString("photoBase64");
                            Intent intent = new Intent(getContext(), ChatDetailActivity.class);
                            intent.putExtra("chatId", chatId);
                            intent.putExtra("otherUserId", otherUserId);
                            intent.putExtra("otherUserName", product.getOwnerName());
                            intent.putExtra("avatarBase64", ownerAvatar);
                            startActivity(intent);
                        });
            } else {
                Toast.makeText(getContext(), "Không thể chat với chính bạn!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPay.setOnClickListener(v ->
                Toast.makeText(getContext(), "Chức năng thanh toán đang phát triển!", Toast.LENGTH_SHORT).show());

        btnReview.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ReviewActivity.class);
            intent.putExtra("productId", product.getId());
            intent.putExtra("sellerId", product.getOwnerId());
            startActivity(intent);
        });
    }

    private void loadProductRating() {
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(product.getId())
                .collection("reviews")
                .get()
                .addOnSuccessListener(query -> {
                    float total = 0;
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        total += doc.getDouble("rating");
                    }
                    float avg = query.size() > 0 ? total / query.size() : 0;
                    productRatingBar.setRating(avg);
                    tvProductReviews.setText("(" + query.size() + " đánh giá)");
                });
    }

    private void loadSellerRating() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(product.getOwnerId())
                .collection("ratings")
                .get()
                .addOnSuccessListener(query -> {
                    float total = 0;
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        total += doc.getDouble("rating");
                    }
                    float avg = query.size() > 0 ? total / query.size() : 0;
                    sellerRatingBar.setRating(avg);
                    tvSellerReviews.setText("(" + query.size() + " đánh giá)");
                });
    }

    private void setupDots(int count) {
        layoutDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(18, 8);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_unselected);
            layoutDots.addView(dot);
        }
        if (count > 0) selectDot(0);
    }

    private void selectDot(int pos) {
        int n = layoutDots.getChildCount();
        for (int i = 0; i < n; i++) {
            View dot = layoutDots.getChildAt(i);
            dot.setBackgroundResource(i == pos ? R.drawable.dot_selected : R.drawable.dot_unselected);
        }
    }

    private class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {
        private final List<String> images;

        public ImageSliderAdapter(List<String> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView img = new ImageView(parent.getContext());
            img.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(img);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            Bitmap bitmap = base64ToBitmap(images.get(position));
            if (bitmap != null)
                holder.imgView.setImageBitmap(bitmap);
            else
                holder.imgView.setImageResource(R.drawable.bg_thumbbar);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imgView;

            ImageViewHolder(View v) {
                super(v);
                imgView = (ImageView) v;
            }
        }
    }

    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}
