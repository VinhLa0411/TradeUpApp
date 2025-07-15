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
import com.example.tradeup.ProductDetailFragment;
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
    private Spinner spinnerFilter, spinnerSort;
    private ArrayAdapter<String> filterAdapter, sortAdapter;

    private final List<String> brandFilterList = new ArrayList<>(Collections.singletonList("Tất cả"));
    private final List<String> sortOptions = Arrays.asList(
            "Mặc định",
            "Giá tăng dần", "Giá giảm dần",
            "Tên A-Z", "Tên Z-A",
            "Mới nhất", "Cũ nhất"
    );

    private String currentSortOption = "Mặc định";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvProducts = view.findViewById(R.id.rvProducts);
        progressBar = view.findViewById(R.id.progressBar);
        searchView = view.findViewById(R.id.searchView);
        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        spinnerSort = view.findViewById(R.id.spinnerSort);

        adapter = new HomeProductAdapter(getContext(), filteredList, product -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product))
                    .addToBackStack(null)
                    .commit();
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);

        // Sắp xếp
        sortAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortOption = sortOptions.get(position);
                filterAndSort();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterAndSort();
                return false;
            }
        });

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSort();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadProducts();
        return view;
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    Set<String> brands = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            p.setOwnerId(doc.getString("ownerId"));
                            p.setOwnerName(doc.getString("ownerName"));
                            if (doc.contains("createdAt")) {
                                p.setCreatedAt(doc.getLong("createdAt"));
                            }
                            if (p.getBrand() != null) brands.add(p.getBrand());
                            productList.add(p);
                        }
                    }

                    brandFilterList.clear();
                    brandFilterList.add("Tất cả");
                    brandFilterList.addAll(brands);

                    filterAdapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, brandFilterList);
                    filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerFilter.setAdapter(filterAdapter);

                    filterAndSort();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    private void filterAndSort() {
        String query = searchView.getQuery() != null ? searchView.getQuery().toString().toLowerCase() : "";
        String selectedBrand = (spinnerFilter.getSelectedItem() != null)
                ? spinnerFilter.getSelectedItem().toString()
                : "Tất cả";

        filteredList.clear();
        for (Product p : productList) {
            boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(query);
            boolean matchBrand = selectedBrand.equals("Tất cả") || selectedBrand.equalsIgnoreCase(p.getBrand());
            if (matchName && matchBrand) {
                filteredList.add(p);
            }
        }

        switch (currentSortOption) {
            case "Giá tăng dần":
                filteredList.sort(Comparator.comparingDouble(Product::getPrice));
                break;
            case "Giá giảm dần":
                filteredList.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case "Tên A-Z":
                filteredList.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Tên Z-A":
                filteredList.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                break;
            case "Mới nhất":
                filteredList.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                break;
            case "Cũ nhất":
                filteredList.sort(Comparator.comparingLong(Product::getCreatedAt));
                break;
        }

        adapter.notifyDataSetChanged();
    }
}
