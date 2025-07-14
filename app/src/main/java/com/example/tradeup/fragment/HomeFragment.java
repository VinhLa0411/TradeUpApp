package com.example.tradeup.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.tradeup.HomeProductAdapter;
import com.example.tradeup.Product;
import com.example.tradeup.ProductDetailFragment; // Import fragment chi tiết
import com.example.tradeup.R;
import com.google.firebase.firestore.*;

import java.util.*;

public class HomeFragment extends Fragment {
    private RecyclerView rvProducts;
    private ProgressBar progressBar;
    private HomeProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> filteredList = new ArrayList<>();
    private SearchView searchView;
    private Spinner spinnerFilter;
    private ArrayAdapter<String> filterAdapter;
    private List<String> brandFilterList = new ArrayList<>(Collections.singletonList("Tất cả"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvProducts = view.findViewById(R.id.rvProducts);
        progressBar = view.findViewById(R.id.progressBar);
        searchView = view.findViewById(R.id.searchView);
        spinnerFilter = view.findViewById(R.id.spinnerFilter);

        // Tạo adapter với callback
        adapter = new HomeProductAdapter(getContext(), filteredList, product -> {
            // Khi bấm "Xem chi tiết"
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product))
                    .addToBackStack(null)
                    .commit();
        });
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);

        filterAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, brandFilterList);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        loadProducts();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts();
                return false;
            }
        });

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterProducts();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    Set<String> ownerIds = new HashSet<>();
                    Set<String> brands = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            String ownerId = doc.getString("ownerId");
                            p.setOwnerId(ownerId);
                            ownerIds.add(ownerId);

                            if (doc.contains("brand") && doc.getString("brand") != null)
                                brands.add(doc.getString("brand"));

                            if (doc.contains("ownerName"))
                                p.setOwnerName(doc.getString("ownerName"));

                            productList.add(p);
                        }
                    }
                    brandFilterList.clear();
                    brandFilterList.add("Tất cả");
                    brandFilterList.addAll(brands);
                    filterAdapter.notifyDataSetChanged();

                    fetchOwnersThenShow(ownerIds);
                })
                .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    private void fetchOwnersThenShow(Set<String> ownerIds) {
        Map<String, String> ownerIdToName = new HashMap<>();
        if (ownerIds.isEmpty()) {
            filterProducts();
            progressBar.setVisibility(View.GONE);
            return;
        }
        FirebaseFirestore.getInstance().collection("users")
                .whereIn(FieldPath.documentId(), new ArrayList<>(ownerIds))
                .get()
                .addOnSuccessListener(userSnaps -> {
                    for (DocumentSnapshot userDoc : userSnaps.getDocuments()) {
                        ownerIdToName.put(userDoc.getId(), userDoc.getString("name"));
                    }
                    for (Product p : productList) {
                        if (p.getOwnerName() == null && ownerIdToName.containsKey(p.getOwnerId())) {
                            p.setOwnerName(ownerIdToName.get(p.getOwnerId()));
                        }
                    }
                    filterProducts();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    filterProducts();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void filterProducts() {
        String query = searchView.getQuery() == null ? "" : searchView.getQuery().toString().toLowerCase();
        String selectedBrand = spinnerFilter.getSelectedItem().toString();

        filteredList.clear();
        for (Product p : productList) {
            boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(query);
            boolean matchBrand = selectedBrand.equals("Tất cả") ||
                    (p.getBrand() != null && p.getBrand().equalsIgnoreCase(selectedBrand));
            if (matchName && matchBrand) {
                filteredList.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
