package com.example.tradeup;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.*;

import com.example.tradeup.R;
import java.util.*;

public class CartPopupFragment extends DialogFragment {
    private RecyclerView rvCartItems;
    private Button btnCheckout;
    private CartAdapter adapter;
    private TextView tvEmpty;

    public static CartPopupFragment newInstance() {
        return new CartPopupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cart_popup, container, false);
        rvCartItems = v.findViewById(R.id.rvCartItems);
        btnCheckout = v.findViewById(R.id.btnCheckout);
        tvEmpty = v.findViewById(R.id.tvEmptyCart);

        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));

        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        adapter = new CartAdapter(cartItems, this::updateUI);
        rvCartItems.setAdapter(adapter);

        updateUI();

        btnCheckout.setOnClickListener(vv -> {
            Toast.makeText(getContext(), "Chức năng thanh toán đang phát triển!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return v;
    }

    private void updateUI() {
        boolean isEmpty = CartManager.getInstance().getCartItems().isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvCartItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
