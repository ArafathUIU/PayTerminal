package com.arafath.payterminalversion2.ui.refund;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.data.repository.PaymentRepository;
import com.arafath.payterminalversion2.util.Money;
import com.arafath.payterminalversion2.util.Time;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RefundSuccessFragment extends Fragment {

    @Inject
    PaymentRepository paymentRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_refund_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String txId = RefundSuccessFragmentArgs.fromBundle(requireArguments()).getTransactionId();
        PaymentTransactionEntity tx = paymentRepository.getById(txId);

        ((TextView) view.findViewById(R.id.refundedAmountText)).setText(Money.format(tx.amountPaise));
        ((TextView) view.findViewById(R.id.transactionIdText)).setText(tx.id);
        ((TextView) view.findViewById(R.id.dateTimeText)).setText(Time.dateTime(tx.refundedAt));

        view.findViewById(R.id.doneButton).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_refund_success_to_home));
    }
}