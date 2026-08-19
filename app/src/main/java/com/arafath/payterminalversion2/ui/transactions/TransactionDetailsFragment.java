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

import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;
import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.util.Money;
import com.arafath.payterminalversion2.util.Time;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionDetailsFragment extends Fragment {

    private TransactionDetailsViewModel viewModel;
    private String transactionId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionDetailsViewModel.class);
        transactionId = TransactionDetailsFragmentArgs.fromBundle(requireArguments()).getTransactionId();

        view.findViewById(R.id.backButton).setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        viewModel.transaction().observe(getViewLifecycleOwner(), this::render);
        viewModel.merchant().observe(getViewLifecycleOwner(), this::renderMerchant);
        viewModel.load(transactionId);
    }

    private void render(PaymentTransactionEntity tx) {
        if (tx == null) {
            return;
        }
        View view = requireView();
        ((TextView) view.findViewById(R.id.amountText)).setText(Money.format(tx.amountPaise));
        ((TextView) view.findViewById(R.id.idValue)).setText(tx.id);
        ((TextView) view.findViewById(R.id.currencyValue)).setText(tx.currency);
        ((TextView) view.findViewById(R.id.methodValue)).setText(tx.method);
        ((TextView) view.findViewById(R.id.terminalValue)).setText(tx.terminalCode);
        ((TextView) view.findViewById(R.id.createdValue)).setText(Time.dateTime(tx.createdAt));
        ((TextView) view.findViewById(R.id.processedValue)).setText(Time.dateTime(tx.processedAt));
        ((TextView) view.findViewById(R.id.referenceValue)).setText(tx.reference);

        TextView status = view.findViewById(R.id.statusText);
        status.setText(tx.status);
        boolean ok = PaymentTransactionEntity.STATUS_SUCCESS.equals(tx.status)
                || PaymentTransactionEntity.STATUS_REFUNDED.equals(tx.status);
        status.setTextColor(ContextCompat.getColor(requireContext(),
                ok ? R.color.success : R.color.error));

        MaterialButton refundButton = view.findViewById(R.id.refundButton);
        boolean refundable = PaymentTransactionEntity.STATUS_SUCCESS.equals(tx.status);
        refundButton.setEnabled(refundable);
        refundButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("transactionId", tx.id);
            Navigation.findNavController(v).navigate(R.id.action_details_to_refund, args);
        });

        view.findViewById(R.id.receiptButton).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("transactionId", tx.id);
            Navigation.findNavController(v).navigate(R.id.action_details_to_receipt, args);
        });
    }

    private void renderMerchant(MerchantEntity merchant) {
        if (merchant != null) {
            TextView merchantValue = requireView().findViewById(R.id.merchantValue);
            merchantValue.setText(merchant.businessName);
        }
    }
}