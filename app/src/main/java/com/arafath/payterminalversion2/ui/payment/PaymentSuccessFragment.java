package com.arafath.payterminalversion2.ui.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.data.repository.PaymentRepository;
import com.arafath.payterminalversion2.util.Money;
import com.arafath.payterminalversion2.util.Time;
import com.google.android.material.button.MaterialButton;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PaymentSuccessFragment extends Fragment {

    @Inject
    PaymentRepository paymentRepository;

    private PaymentTransactionEntity transaction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String txId = PaymentSuccessFragmentArgs.fromBundle(requireArguments()).getTransactionId();

        paymentRepository.observeById(txId).observe(getViewLifecycleOwner(), this::render);
    }

    private void render(PaymentTransactionEntity transaction) {
        if (transaction == null) {
            return;
        }
        this.transaction = transaction;
        View view = requireView();
        ((TextView) view.findViewById(R.id.amountText)).setText(Money.format(transaction.amountPaise));
        ((TextView) view.findViewById(R.id.transactionIdText)).setText(transaction.id);
        ((TextView) view.findViewById(R.id.methodText)).setText(transaction.method);
        ((TextView) view.findViewById(R.id.referenceText)).setText(transaction.reference);
        ((TextView) view.findViewById(R.id.dateTimeText)).setText(Time.dateTime(transaction.createdAt));
        ((TextView) view.findViewById(R.id.terminalText)).setText(transaction.terminalCode);

        MaterialButton receiptButton = view.findViewById(R.id.viewReceiptButton);
        receiptButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("transactionId", transaction.id);
            Navigation.findNavController(v).navigate(R.id.action_success_to_receipt, args);
        });

        MaterialButton newPaymentButton = view.findViewById(R.id.newPaymentButton);
        newPaymentButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_success_to_new_payment));
    }
}