package com.arafath.payterminalversion2.ui.payment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
public class CardPaymentFragment extends Fragment {

    private long amountPaise;
    private TextInputEditText cardNumberInput;
    private TextView maskedCardText;
    private TextView cardHolderText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        amountPaise = CardPaymentFragmentArgs.fromBundle(requireArguments()).getAmountPaise();
        ((TextView) view.findViewById(R.id.amountText)).setText(Money.format(amountPaise));

        cardNumberInput = view.findViewById(R.id.cardNumberInput);
        maskedCardText = view.findViewById(R.id.maskedCardText);
        cardHolderText = view.findViewById(R.id.cardHolderText);
        TextInputEditText cardHolderInput = view.findViewById(R.id.cardHolderInput);

        cardNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                maskedCardText.setText(mask(s.toString()));
            }
        });

        TextInputEditText expiryInput = view.findViewById(R.id.expiryInput);
        expiryInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String raw = s.toString().replaceAll("\\D", "");
                String formatted;
                if (raw.length() <= 2) {
                    formatted = raw;
                } else if (raw.length() <= 4) {
                    formatted = raw.substring(0, 2) + "/" + raw.substring(2);
                } else {
                    formatted = raw.substring(0, 2) + "/" + raw.substring(2, 4);
                }
                expiryInput.setText(formatted);
                expiryInput.setSelection(formatted.length());
            }
        });

        cardHolderInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                cardHolderText.setText(s.toString().toUpperCase());
            }
        });

        MaterialButton processButton = view.findViewById(R.id.processButton);
        processButton.setOnClickListener(v -> {
            String number = digitsOnly(cardNumberInput.getText());
            String expiry = ((TextInputEditText) view.findViewById(R.id.expiryInput)).getText() == null
                    ? "" : ((TextInputEditText) view.findViewById(R.id.expiryInput)).getText().toString();
            String cvv = ((TextInputEditText) view.findViewById(R.id.cvvInput)).getText() == null
                    ? "" : ((TextInputEditText) view.findViewById(R.id.cvvInput)).getText().toString();
            if (number.length() < 12 || expiry.isEmpty() || cvv.isEmpty()) {
                cardNumberInput.setError("Enter a valid card number");
                return;
            }
            navigateToProcessing(mask(number));
        });
    }

    private void navigateToProcessing(String masked) {
        NavController nav = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putLong("amountPaise", amountPaise);
        args.putString("method", "CARD");
        args.putString("maskedRef", masked);
        nav.navigate(R.id.action_card_to_processing, args);
    }

    private String digitsOnly(Editable s) {
        return s == null ? "" : s.toString().replaceAll("\\D", "");
    }

    private String mask(String digits) {
        String d = digits.replaceAll("\\D", "");
        if (d.isEmpty()) {
            return "•••• •••• •••• ••••";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 4 || i == 8 || i == 12) {
                sb.append(' ');
            }
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }
}