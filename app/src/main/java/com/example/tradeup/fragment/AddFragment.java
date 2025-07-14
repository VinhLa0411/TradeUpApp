package com.example.tradeup.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.tradeup.Product;
import com.example.tradeup.ProductAdapter;
import com.example.tradeup.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class AddFragment extends Fragment {
    private RecyclerView rvMyProducts;
    private ProductAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private FloatingActionButton fabAddProduct;
    private FirebaseFirestore db;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(com.example.tradeup.R.layout.fragment_add, container, false);

        rvMyProducts = view.findViewById(com.example.tradeup.R.id.recyclerProducts);
        fabAddProduct = view.findViewById(com.example.tradeup.R.id.fabAddProduct);

        rvMyProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductAdapter(getContext(), products, new ProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                // Mở màn hình chỉnh sửa sản phẩm (gửi id sản phẩm và data qua Bundle)
                Bundle bundle = new Bundle();
                bundle.putString("productId", product.getId());
                bundle.putString("name", product.getName());
                bundle.putInt("quantity", product.getQuantity());
                bundle.putString("brand", product.getBrand());
                bundle.putInt("year", product.getYear());
                bundle.putDouble("price", product.getPrice());
                bundle.putString("description", product.getDescription());
                bundle.putStringArrayList("images", product.getImages() == null ? new ArrayList<>() : new ArrayList<>(product.getImages()));

                AddProductFormFragment fragment = new AddProductFormFragment();
                fragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(com.example.tradeup.R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDelete(Product product) {
                confirmDeleteProduct(product);
            }
        });
        rvMyProducts.setAdapter(adapter);

        fabAddProduct.setOnClickListener(v -> {
            // Mở form đăng sản phẩm mới (không truyền Bundle)
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddProductFormFragment())
                    .addToBackStack(null)
                    .commit();
        });

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadMyProducts();

        return view;
    }

    private void loadMyProducts() {
        db.collection("products")
                .whereEqualTo("ownerId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    products.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            products.add(p);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void confirmDeleteProduct(Product product) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn chắc chắn muốn xóa sản phẩm này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteProduct(Product product) {
        db.collection("products").document(product.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã xóa sản phẩm!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
