package com.example.tradeup;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class TransactionHistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TransactionAdapter adapter;
    private final List<Transaction> transactionList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transaction_history, container, false);

        recyclerView = view.findViewById(R.id.rvTransactions);
        progressBar = view.findViewById(R.id.progressBar);

        adapter = new TransactionAdapter(requireContext(), transactionList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadTransactions();

        return view;
    }

    private void loadTransactions() {
        progressBar.setVisibility(View.VISIBLE);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    transactionList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) {
                            transactionList.add(t);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Lỗi tải giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
