package com.arafath.payterminalversion2.ui.pair;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arafath.payterminalversion2.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PairTerminalFragment extends Fragment {

    private PairTerminalViewModel viewModel;
    private TextInputEditText pairingCodeInput;
    private TextInputEditText terminalNameInput;
    private MaterialButton pairButton;
    private ProgressBar progress;
    private TextView errorText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pair_terminal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PairTerminalViewModel.class);

        pairingCodeInput = view.findViewById(R.id.pairingCodeInput);
        terminalNameInput = view.findViewById(R.id.terminalNameInput);
        pairButton = view.findViewById(R.id.pairButton);
        progress = view.findViewById(R.id.progress);
        errorText = view.findViewById(R.id.errorText);

        pairButton.setOnClickListener(v -> {
            errorText.setVisibility(View.GONE);
            viewModel.pair(
                    text(pairingCodeInput),
                    text(terminalNameInput));
        });

        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            pairButton.setEnabled(!loading);
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error == null) {
                return;
            }
            errorText.setText(error);
            errorText.setVisibility(View.VISIBLE);
        });
    }

    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}