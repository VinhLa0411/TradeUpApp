package com.example.tradeup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.*;

public class CartBottomSheetDialog extends BottomSheetDialogFragment {
    private LinearLayout layoutCartItems;
    private Button btnCheckout;
    private TextView tvTotalPrice;

    private Runnable onCartChangedListener;

    public void setOnCartChangedListener(Runnable listener) {
        this.onCartChangedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_cart_bottom_sheet, container, false);
        layoutCartItems = v.findViewById(R.id.layoutCartItems);
        btnCheckout = v.findViewById(R.id.btnCheckout);
        tvTotalPrice = v.findViewById(R.id.tvTotalPrice);

        renderCartItems();

        btnCheckout.setOnClickListener(view -> {
            List<CartItem> items = CartManager.getInstance().getCartItems();
            if (items.isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String buyerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            int successCount = 0;

            for (CartItem item : items) {
                Product product = item.getProduct();
                int buyQty = item.getQuantity();
                int newQty = product.getQuantity() - buyQty;

                if (newQty < 0) {
                    Toast.makeText(getContext(), "Không đủ hàng cho: " + product.getName(), Toast.LENGTH_SHORT).show();
                    continue;
                }

                db.collection("products").document(product.getId())
                        .update("quantity", newQty);

                Map<String, Object> transaction = new HashMap<>();
                transaction.put("productId", product.getId());
                transaction.put("productName", product.getName());
                transaction.put("price", product.getPrice());
                transaction.put("quantity", buyQty);
                transaction.put("total", product.getPrice() * buyQty);
                transaction.put("timestamp", System.currentTimeMillis());

                // Quan trọng: key phải là "imageBase64" để Transaction.java đọc được
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    transaction.put("imageBase64", product.getImages().get(0));
                }

                db.collection("users")
                        .document(buyerId)
                        .collection("transactions")
                        .add(transaction);

                successCount++;
            }

            if (successCount > 0) {
                Toast.makeText(getContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                CartManager.getInstance().clearCart();
                notifyCartChanged();
                dismiss();
            }
        });

        return v;
    }

    private void renderCartItems() {
        layoutCartItems.removeAllViews();
        List<CartItem> items = CartManager.getInstance().getCartItems();
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        double totalPrice = 0;

        for (CartItem item : new ArrayList<>(items)) {
            View row = LayoutInflater.from(getContext()).inflate(R.layout.item_cart_row, layoutCartItems, false);

            ImageView img = row.findViewById(R.id.imgProduct);
            TextView name = row.findViewById(R.id.tvName);
            TextView price = row.findViewById(R.id.tvPrice);
            TextView qty = row.findViewById(R.id.tvQuantity);
            ImageButton btnPlus = row.findViewById(R.id.btnPlus);
            ImageButton btnMinus = row.findViewById(R.id.btnMinus);
            ImageButton btnRemove = row.findViewById(R.id.btnRemove);

            name.setText(item.getProduct().getName());
            price.setText(nf.format(item.getProduct().getPrice()) + " đ");
            qty.setText(String.valueOf(item.getQuantity()));

            totalPrice += item.getProduct().getPrice() * item.getQuantity();

            if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                Bitmap bm = decodeBase64(item.getProduct().getImages().get(0));
                if (bm != null) img.setImageBitmap(bm);
                else img.setImageResource(R.drawable.ic_add_photo);
            } else {
                img.setImageResource(R.drawable.ic_add_photo);
            }

            btnPlus.setOnClickListener(v -> {
                int current = item.getQuantity();
                int max = item.getProduct().getQuantity();
                if (current < max) {
                    CartManager.getInstance().increaseQuantity(item.getProduct());
                    qty.setText(String.valueOf(item.getQuantity()));
                    notifyCartChanged();
                } else {
                    Toast.makeText(getContext(), "Chỉ còn " + max + " sản phẩm", Toast.LENGTH_SHORT).show();
                }
            });

            btnMinus.setOnClickListener(v -> {
                CartManager.getInstance().decreaseQuantity(item.getProduct());
                if (item.getQuantity() <= 0) {
                    layoutCartItems.removeView(row);
                } else {
                    qty.setText(String.valueOf(item.getQuantity()));
                }
                notifyCartChanged();
            });

            btnRemove.setOnClickListener(v -> {
                CartManager.getInstance().removeFromCart(item.getProduct());
                layoutCartItems.removeView(row);
                notifyCartChanged();
            });

            layoutCartItems.addView(row);
        }

        if (tvTotalPrice != null) {
            tvTotalPrice.setText("Tổng tiền: " + nf.format(totalPrice) + " đ");
        }
    }

    private void notifyCartChanged() {
        renderCartItems();
        if (onCartChangedListener != null) {
            onCartChangedListener.run();
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
