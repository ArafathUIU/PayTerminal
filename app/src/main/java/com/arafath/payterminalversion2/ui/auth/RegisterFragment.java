package com.arafath.payterminalversion2.ui.auth;

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
import androidx.navigation.fragment.NavHostFragment;

import com.arafath.payterminalversion2.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private RegisterViewModel viewModel;
    private TextInputEditText nameInput;
    private TextInputEditText businessInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText phoneInput;
    private MaterialButton registerButton;
    private ProgressBar progress;
    private TextView errorText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        nameInput = view.findViewById(R.id.nameInput);
        businessInput = view.findViewById(R.id.businessInput);
        emailInput = view.findViewById(R.id.regEmailInput);
        passwordInput = view.findViewById(R.id.regPasswordInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        registerButton = view.findViewById(R.id.registerButton);
        progress = view.findViewById(R.id.progress);
        errorText = view.findViewById(R.id.errorText);

        registerButton.setOnClickListener(v -> {
            errorText.setVisibility(View.GONE);
            viewModel.register(
                    text(nameInput),
                    text(businessInput),
                    text(emailInput),
                    text(passwordInput),
                    text(phoneInput));
        });

        view.findViewById(R.id.goToLoginButton).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_register_to_login));

        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            registerButton.setEnabled(!loading);
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