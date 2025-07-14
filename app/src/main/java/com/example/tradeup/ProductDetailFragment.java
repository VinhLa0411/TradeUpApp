package com.example.tradeup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import android.text.TextUtils;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.text.NumberFormat;
import java.util.*;

public class ProductDetailFragment extends Fragment {
    private ViewPager2 viewPagerImages;
    private LinearLayout layoutDots;
    private TextView tvName, tvPrice, tvBrand, tvYear, tvQuantity, tvOwner, tvDescription;
    private Button btnContact, btnPay;

    private Product product;
    private List<String> imageBase64s = new ArrayList<>();

    public ProductDetailFragment() {}

    // Nhận Product qua Bundle (Product cần implements Serializable)
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
        btnContact = view.findViewById(R.id.btnContact);
        btnPay = view.findViewById(R.id.btnPay);

        if (product != null) {
            showProductInfo();
        }

        return view;
    }

    private void showProductInfo() {
        // Slider ảnh
        viewPagerImages.setAdapter(new ImageSliderAdapter(imageBase64s));
        setupDots(imageBase64s.size());
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) { selectDot(position); }
        });

        // Thông tin sản phẩm
        tvName.setText(product.getName());
        tvBrand.setText("Hãng: " + product.getBrand());
        tvYear.setText("Năm sản xuất: " + product.getYear());
        tvQuantity.setText("Số lượng: " + product.getQuantity());

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvPrice.setText("Giá: " + nf.format(product.getPrice()) + " VNĐ");

        if (!TextUtils.isEmpty(product.getOwnerName()))
            tvOwner.setText("Người đăng: " + product.getOwnerName());
        else
            tvOwner.setText("Người đăng: (không rõ)");

        // Mô tả sản phẩm (nếu có)
        if (product.getDescription() != null && !product.getDescription().isEmpty())
            tvDescription.setText(product.getDescription());
        else
            tvDescription.setText("(Không có mô tả)");

        //Nut lien he
        // ... [Các phần trên giữ nguyên] ...

        btnContact.setOnClickListener(v -> {
            if (product.getOwnerId() != null && !product.getOwnerId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String otherUserId = product.getOwnerId();
                String chatId = (currentUserId.compareTo(otherUserId) < 0)
                        ? currentUserId + "_" + otherUserId : otherUserId + "_" + currentUserId;

                // Lấy avatar chủ sản phẩm
                FirebaseFirestore.getInstance().collection("users")
                        .document(otherUserId)
                        .get()
                        .addOnSuccessListener(userDoc -> {
                            String ownerAvatar = "";
                            if (userDoc.exists()) {
                                ownerAvatar = userDoc.getString("photoBase64");
                            }
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



        // Nút thanh toán (demo)
        btnPay.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng thanh toán đang phát triển!", Toast.LENGTH_SHORT).show();
        });
    }

    // --------- SLIDER DOTS -----------
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

    // --------- ADAPTER SLIDER ẢNH ----------
    private class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {
        private List<String> images;
        public ImageSliderAdapter(List<String> images) { this.images = images; }
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
        public int getItemCount() { return images.size(); }
        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imgView;
            ImageViewHolder(View v) { super(v); imgView = (ImageView) v; }
        }
    }
    // Convert base64 -> Bitmap
    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) { return null; }
    }
}
