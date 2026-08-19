package com.arafath.payterminalversion2.ui.refund;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.util.Money;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RefundFragment extends Fragment {

    private RefundViewModel viewModel;
    private String transactionId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_refund, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RefundViewModel.class);
        transactionId = RefundFragmentArgs.fromBundle(requireArguments()).getTransactionId();

        ((TextView) view.findViewById(R.id.transactionIdText)).setText(transactionId);
        viewModel.load(transactionId);

        MaterialButton confirmButton = view.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            TextInputEditText amountInput = view.findViewById(R.id.refundAmountInput);
            String amountText = amountInput.getText() == null ? "" : amountInput.getText().toString().trim();
            TextInputEditText reasonInput = view.findViewById(R.id.refundReasonInput);
            String reason = reasonInput.getText() == null ? "" : reasonInput.getText().toString().trim();
            if (amountText.isEmpty()) {
                amountInput.setError("Enter a refund amount");
                return;
            }
            long refundPaise;
            try {
                refundPaise = Long.parseLong(amountText);
            } catch (NumberFormatException e) {
                amountInput.setError("Invalid amount");
                return;
            }
            viewModel.confirm(refundPaise, reason);
        });

        viewModel.originalAmount().observe(getViewLifecycleOwner(),
                amount -> ((TextView) view.findViewById(R.id.originalAmountText)).setText(Money.format(amount)));

        viewModel.refunded().observe(getViewLifecycleOwner(), refunded -> {
            if (refunded != null && refunded) {
                NavController nav = Navigation.findNavController(requireView());
                Bundle args = new Bundle();
                args.putString("transactionId", transactionId);
                nav.navigate(R.id.action_refund_to_success, args);
            }
        });

        viewModel.error().observe(getViewLifecycleOwner(),
                error -> { if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show(); });
    }
}