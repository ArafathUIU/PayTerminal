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
public class LoginFragment extends Fragment {

    private LoginViewModel viewModel;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private ProgressBar progress;
    private TextView errorText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        loginButton = view.findViewById(R.id.loginButton);
        progress = view.findViewById(R.id.progress);
        errorText = view.findViewById(R.id.errorText);

        loginButton.setOnClickListener(v -> {
            errorText.setVisibility(View.GONE);
            viewModel.login(
                    text(emailInput),
                    text(passwordInput));
        });

        view.findViewById(R.id.goToRegisterButton).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register));

        viewModel.loading.observe(getViewLifecycleOwner(), loading -> {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            loginButton.setEnabled(!loading);
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