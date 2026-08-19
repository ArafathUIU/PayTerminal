package com.arafath.payterminalversion2.ui.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.util.Money;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NewPaymentFragment extends Fragment {

    private NewPaymentViewModel viewModel;
    private TextView amountText;
    private TextView terminalStatusText;
    private TextView[] keys;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NewPaymentViewModel.class);

        amountText = view.findViewById(R.id.amountText);
        terminalStatusText = view.findViewById(R.id.terminalStatusText);

        keys = new TextView[]{
                view.findViewById(R.id.key1),
                view.findViewById(R.id.key2),
                view.findViewById(R.id.key3),
                view.findViewById(R.id.key4),
                view.findViewById(R.id.key5),
                view.findViewById(R.id.key6),
                view.findViewById(R.id.key7),
                view.findViewById(R.id.key8),
                view.findViewById(R.id.key9),
                view.findViewById(R.id.key0),
        };

        for (int i = 0; i < 9; i++) {
            final String digit = String.valueOf(i + 1);
            keys[i].setOnClickListener(v -> viewModel.appendDigit(digit));
        }
        keys[9].setOnClickListener(v -> viewModel.appendDigit("0"));
        view.findViewById(R.id.keyClear).setOnClickListener(v -> viewModel.clear());
        view.findViewById(R.id.keyBack).setOnClickListener(v -> viewModel.backspace());

        MaterialButton continueButton = view.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v -> {
            long amount = viewModel.amountPaise();
            if (amount > 0) {
                NavController nav = Navigation.findNavController(v);
                Bundle args = new Bundle();
                args.putLong("amountPaise", amount);
                nav.navigate(R.id.action_new_payment_to_method, args);
            }
        });

        viewModel.amountPaise.observe(getViewLifecycleOwner(), this::renderAmount);
        viewModel.terminalLabel.observe(getViewLifecycleOwner(), label ->
                terminalStatusText.setText(label == null ? "" : label));
    }

    private void renderAmount(long paise) {
        amountText.setText(Money.format(paise));
    }
}