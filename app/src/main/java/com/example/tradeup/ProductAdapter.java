package com.example.tradeup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> list;
    private Context context;
    private OnProductActionListener actionListener;

    // Callback cho hai nút
    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    public ProductAdapter(Context context, List<Product> list, OnProductActionListener listener) {
        this.context = context;
        this.list = list;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = list.get(position);
        holder.tvName.setText(p.getName());
        holder.tvBrand.setText("Hãng: " + p.getBrand());
        holder.tvYear.setText("Năm sản xuất: " + p.getYear());
        holder.tvQuantity.setText("Số lượng: " + p.getQuantity());

        // Format giá tiền: 12.000.000 VNĐ
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText("Giá: " + nf.format(p.getPrice()) + " VNĐ");

        // Hiển thị ảnh sản phẩm (ảnh đầu tiên)
        List<String> images = p.getImages();
        if (images != null && !images.isEmpty()) {
            // Ảnh đầu tiên là đại diện
            Bitmap bitmap = base64ToBitmap(images.get(0));
            if (bitmap != null) {
                holder.imgProduct.setImageBitmap(bitmap);
            } else {
                holder.imgProduct.setImageResource(R.drawable.bg_thumbbar);
            }
        } else {
            holder.imgProduct.setImageResource(R.drawable.bg_thumbbar);
        }

        // Nút Sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEdit(p);
        });

        // Nút Xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDelete(p);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // ViewHolder cho mỗi item
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvBrand, tvYear, tvQuantity, tvPrice;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View v) {
            super(v);
            imgProduct = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.tvName);
            tvBrand = v.findViewById(R.id.tvBrand);
            tvYear = v.findViewById(R.id.tvYear);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            tvPrice = v.findViewById(R.id.tvPrice);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }

    // Chuyển base64 sang Bitmap
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
