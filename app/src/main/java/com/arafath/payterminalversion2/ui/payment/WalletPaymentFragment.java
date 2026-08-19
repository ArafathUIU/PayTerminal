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
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WalletPaymentFragment extends Fragment {

    private long amountPaise;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        amountPaise = WalletPaymentFragmentArgs.fromBundle(requireArguments()).getAmountPaise();
        ((TextView) view.findViewById(R.id.amountText)).setText(Money.format(amountPaise));
        ((TextView) view.findViewById(R.id.confirmAmountText)).setText(Money.format(amountPaise));

        MaterialButton processButton = view.findViewById(R.id.processButton);
        processButton.setOnClickListener(v -> {
            TextInputEditText mobileInput = view.findViewById(R.id.mobileInput);
            String mobile = mobileInput.getText() == null ? "" : mobileInput.getText().toString().trim();
            if (mobile.length() < 10) {
                mobileInput.setError("Enter a valid mobile number");
                return;
            }
            NavController nav = Navigation.findNavController(v);
            Bundle args = new Bundle();
            args.putLong("amountPaise", amountPaise);
            args.putString("method", "WALLET");
            args.putString("maskedRef", "****" + mobile.substring(mobile.length() - 4));
            nav.navigate(R.id.action_wallet_to_processing, args);
        });
    }
}