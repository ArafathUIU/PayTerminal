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
import com.google.android.material.button.MaterialButton;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PaymentFailedFragment extends Fragment {

    @Inject
    PaymentRepository paymentRepository;

    private PaymentTransactionEntity transaction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_failed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String txId = PaymentFailedFragmentArgs.fromBundle(requireArguments()).getTransactionId();
        transaction = paymentRepository.getById(txId);

        ((TextView) view.findViewById(R.id.amountText)).setText(Money.format(transaction.amountPaise));
        ((TextView) view.findViewById(R.id.transactionIdText)).setText(transaction.id);

        MaterialButton retryButton = view.findViewById(R.id.retryButton);
        retryButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_failed_to_retry));

        MaterialButton cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_failed_to_home));
    }
}