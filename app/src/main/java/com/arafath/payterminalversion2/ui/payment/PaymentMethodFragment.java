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

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PaymentMethodFragment extends Fragment {

    private long amountPaise;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        amountPaise = PaymentMethodFragmentArgs.fromBundle(requireArguments()).getAmountPaise();

        ((TextView) view.findViewById(R.id.amountText)).setText(Money.format(amountPaise));

        view.findViewById(R.id.cardMethod).setOnClickListener(v -> navigateWithAmount(R.id.action_method_to_card));
        view.findViewById(R.id.qrMethod).setOnClickListener(v -> navigateWithAmount(R.id.action_method_to_qr));
        view.findViewById(R.id.walletMethod).setOnClickListener(v -> navigateWithAmount(R.id.action_method_to_wallet));
    }

    private void navigateWithAmount(int actionId) {
        NavController nav = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putLong("amountPaise", amountPaise);
        nav.navigate(actionId, args);
    }
}