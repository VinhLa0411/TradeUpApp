package com.example.tradeup;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeup.R;

import java.text.NumberFormat;
import java.util.*;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartVH> {

    private final List<CartItem> cartItems;
    private final Runnable onCartChanged;

    public CartAdapter(List<CartItem> cartItems, Runnable onCartChanged) {
        this.cartItems = cartItems;
        this.onCartChanged = onCartChanged;
    }

    @NonNull
    @Override
    public CartVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_popup, parent, false);
        return new CartVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartVH h, int position) {
        CartItem item = cartItems.get(position);
        h.tvName.setText(item.getProduct().getName());

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        h.tvPrice.setText(nf.format(item.getProduct().getPrice()) + " đ");
        h.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Tăng số lượng
        h.btnPlus.setOnClickListener(v -> {
            CartManager.getInstance().increaseQuantity(item.getProduct());
            h.tvQuantity.setText(String.valueOf(item.getQuantity()));
            notifyItemChanged(position);
            onCartChanged.run();
        });

        // Giảm số lượng
        h.btnMinus.setOnClickListener(v -> {
            CartManager.getInstance().decreaseQuantity(item.getProduct());
            if (item.getQuantity() <= 0) {
                cartItems.remove(position);
                notifyItemRemoved(position);
            } else {
                h.tvQuantity.setText(String.valueOf(item.getQuantity()));
                notifyItemChanged(position);
            }
            onCartChanged.run();
        });

        // Xóa sản phẩm
        h.btnRemove.setOnClickListener(v -> {
            CartManager.getInstance().removeFromCart(item.getProduct());
            cartItems.remove(position);
            notifyItemRemoved(position);
            onCartChanged.run();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartVH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        ImageButton btnPlus, btnMinus;
        Button btnRemove;

        public CartVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartName);
            tvPrice = itemView.findViewById(R.id.tvCartPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
