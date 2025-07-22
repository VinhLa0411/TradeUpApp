package com.example.tradeup;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionVH> {
    private final List<Transaction> list;
    private final Context context;

    public TransactionAdapter(Context context, List<Transaction> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TransactionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionVH h, int position) {
        Transaction t = list.get(position);
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        h.tvName.setText(t.getProductName());
        h.tvPrice.setText("Giá: " + nf.format(t.getPrice()) + " đ");
        h.tvQuantity.setText("Số lượng: " + t.getQuantity());
        h.tvTime.setText("Thời gian: " + sdf.format(new Date(t.getTimestamp())));
        double total = t.getTotal() > 0 ? t.getTotal() : t.getPrice() * t.getQuantity();
        h.tvTotal.setText("Tổng tiền: " + nf.format(total) + " đ");

        if (t.getImageBase64() != null && !t.getImageBase64().isEmpty()) {
            Bitmap bm = decodeBase64(t.getImageBase64());
            if (bm != null) {
                h.imgProduct.setImageBitmap(bm);
            } else {
                h.imgProduct.setImageResource(R.drawable.ic_add_photo);
            }
        } else {
            h.imgProduct.setImageResource(R.drawable.ic_add_photo);
        }

        // Xử lý nút xoá giao dịch
        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xoá giao dịch")
                    .setMessage("Bạn có chắc muốn xoá giao dịch này?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .collection("transactions")
                                .whereEqualTo("timestamp", t.getTimestamp())
                                .limit(1)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    if (!snapshot.isEmpty()) {
                                        snapshot.getDocuments().get(0).getReference().delete();
                                        list.remove(position);
                                        notifyItemRemoved(position);
                                        Toast.makeText(context, "Đã xoá giao dịch", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class TransactionVH extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice, tvTime, tvTotal;
        ImageView imgProduct;
        ImageButton btnDelete;

        public TransactionVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            imgProduct = itemView.findViewById(R.id.imgProduct);
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
