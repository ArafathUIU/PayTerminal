package com.arafath.payterminalversion2.ui.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.util.Money;
import com.arafath.payterminalversion2.util.Time;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionHistoryFragment extends Fragment {

    private TransactionHistoryViewModel viewModel;
    private TransactionAdapter adapter;
    private TextView emptyText;

    private final List<MaterialButton> filterChips = new java.util.ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionHistoryViewModel.class);

        RecyclerView list = view.findViewById(R.id.transactionList);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(this::onTransactionClicked);
        list.setAdapter(adapter);
        emptyText = view.findViewById(R.id.emptyText);

        view.findViewById(R.id.backButton).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_history_to_home));

        TextInputEditText searchInput = view.findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                viewModel.setQuery(s == null ? "" : s.toString());
            }
        });

        bindChip(view, R.id.filterAll, "ALL", "method");
        bindChip(view, R.id.filterCard, "CARD", "method");
        bindChip(view, R.id.filterQr, "QR", "method");
        bindChip(view, R.id.filterWallet, "WALLET", "method");
        bindChip(view, R.id.filterSuccess, "SUCCESS", "status");
        bindChip(view, R.id.filterFailed, "FAILED", "status");

        viewModel.transactions().observe(getViewLifecycleOwner(), this::render);
    }

    private void bindChip(View root, int id, String value, String kind) {
        MaterialButton chip = root.findViewById(id);
        filterChips.add(chip);
        chip.setOnClickListener(v -> {
            for (MaterialButton c : filterChips) {
                c.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.white)));
                c.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
            }
            chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.orange)));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            if ("method".equals(kind)) {
                viewModel.setMethodFilter(value);
            } else {
                viewModel.setStatusFilter(value);
            }
        });
    }

    private void render(List<PaymentTransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
        adapter.submit(transactions);
    }

    private void onTransactionClicked(PaymentTransactionEntity tx) {
        NavController nav = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putString("transactionId", tx.id);
        nav.navigate(R.id.action_history_to_details, args);
    }

    private static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.Holder> {
        private final List<PaymentTransactionEntity> items = new java.util.ArrayList<>();
        private final OnClick onClick;

        interface OnClick {
            void onItem(PaymentTransactionEntity tx);
        }

        TransactionAdapter(OnClick onClick) {
            this.onClick = onClick;
        }

        void submit(List<PaymentTransactionEntity> list) {
            items.clear();
            if (list != null) {
                items.addAll(list);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            PaymentTransactionEntity tx = items.get(position);
            holder.idText.setText(tx.id);
            holder.amountText.setText(Money.format(tx.amountPaise));
            holder.metaText.setText(tx.method + " · " + Time.dateTime(tx.createdAt));
            holder.statusText.setText(tx.status);
            boolean success = PaymentTransactionEntity.STATUS_SUCCESS.equals(tx.status)
                    || PaymentTransactionEntity.STATUS_REFUNDED.equals(tx.status);
            holder.statusText.setTextColor(android.graphics.Color.parseColor(
                    success ? "#2E7D32" : "#C62828"));
            holder.itemView.setOnClickListener(v -> onClick.onItem(tx));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView idText;
            final TextView amountText;
            final TextView metaText;
            final TextView statusText;

            Holder(@NonNull View itemView) {
                super(itemView);
                idText = itemView.findViewById(R.id.txIdText);
                amountText = itemView.findViewById(R.id.txAmountText);
                metaText = itemView.findViewById(R.id.txMetaText);
                statusText = itemView.findViewById(R.id.txStatusText);
            }
        }
    }
}