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
import com.arafath.payterminalversion2.util.Money;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QrPaymentFragment extends Fragment {

    private long amountPaise;
    private TextView paymentStatusText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        amountPaise = QrPaymentFragmentArgs.fromBundle(requireArguments()).getAmountPaise();
        ((TextView) view.findViewById(R.id.amountText)).setText(Money.format(amountPaise));
        paymentStatusText = view.findViewById(R.id.paymentStatusText);

        MaterialButton simulateButton = view.findViewById(R.id.simulateScanButton);
        simulateButton.setOnClickListener(v -> {
            simulateButton.setEnabled(false);
            paymentStatusText.setText("QR code captured");
        });

        MaterialButton processButton = view.findViewById(R.id.processButton);
        processButton.setOnClickListener(v -> {
            NavController nav = Navigation.findNavController(v);
            Bundle args = new Bundle();
            args.putLong("amountPaise", amountPaise);
            args.putString("method", "QR");
            args.putString("maskedRef", "QR-CAPTURED");
            nav.navigate(R.id.action_qr_to_processing, args);
        });
    }
}