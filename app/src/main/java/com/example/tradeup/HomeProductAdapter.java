package com.example.tradeup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HomeProductAdapter extends RecyclerView.Adapter<HomeProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private Context context;
    private OnProductDetailListener detailListener;

    public interface OnProductDetailListener {
        void onDetail(Product product);
    }

    public HomeProductAdapter(Context context, List<Product> productList, OnProductDetailListener detailListener) {
        this.context = context;
        this.productList = productList;
        this.detailListener = detailListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_home_product, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = productList.get(position);

        holder.tvName.setText(p.getName());
        holder.tvBrand.setText("Hãng: " + p.getBrand());
        holder.tvYear.setText("Năm sản xuất: " + p.getYear());
        holder.tvQuantity.setText("Số lượng: " + p.getQuantity());

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        String priceFormatted = nf.format(p.getPrice()) + " VNĐ";
        holder.tvPrice.setText("Giá: " + priceFormatted);

        holder.tvOwner.setText("Người đăng: " + (p.getOwnerName() != null ? p.getOwnerName() : "Không rõ"));

        // Hiển thị lượt xem
        holder.tvViews.setText("👁 " + p.getViews());

        List<String> images = p.getImages();
        if (images != null && !images.isEmpty()) {
            Bitmap bitmap = base64ToBitmap(images.get(0));
            if (bitmap != null) {
                holder.imgProduct.setImageBitmap(bitmap);
            } else {
                holder.imgProduct.setImageResource(R.drawable.bg_thumbbar);
            }
        } else {
            holder.imgProduct.setImageResource(R.drawable.bg_thumbbar);
        }

        holder.btnDetail.setOnClickListener(v -> {
            // Tăng views Firestore (tăng trước khi mở chi tiết)
            int newViews = p.getViews() + 1;
            FirebaseFirestore.getInstance()
                    .collection("products")
                    .document(p.getId())
                    .update("views", newViews);

            p.setViews(newViews);
            notifyItemChanged(position);

            if (detailListener != null) detailListener.onDetail(p);
        });

        // Optional: click cả card
        holder.itemView.setOnClickListener(v -> {
            int newViews = p.getViews() + 1;
            FirebaseFirestore.getInstance()
                    .collection("products")
                    .document(p.getId())
                    .update("views", newViews);

            p.setViews(newViews);
            notifyItemChanged(position);

            if (detailListener != null) detailListener.onDetail(p);
        });
    }

    @Override
    public int getItemCount() { return productList.size(); }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvBrand, tvYear, tvQuantity, tvPrice, tvOwner, tvViews;
        Button btnDetail;

        public ProductViewHolder(@NonNull View v) {
            super(v);
            imgProduct = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.tvName);
            tvBrand = v.findViewById(R.id.tvBrand);
            tvYear = v.findViewById(R.id.tvYear);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvOwner = v.findViewById(R.id.tvOwner);
            btnDetail = v.findViewById(R.id.btnDetail);
            tvViews = v.findViewById(R.id.tvViews); // TextView mới hiển thị lượt xem
        }
    }

    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
