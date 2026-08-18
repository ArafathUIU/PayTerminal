package com.arafath.payterminalversion2.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private TextView greetingText;
    private TextView userEmailText;
    private TextView userRoleText;
    private TextView terminalCodeText;
    private TextView terminalNameText;
    private TextView terminalStatusText;
    private TextView merchantText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        greetingText = view.findViewById(R.id.greetingText);
        userEmailText = view.findViewById(R.id.userEmailText);
        userRoleText = view.findViewById(R.id.userRoleText);
        terminalCodeText = view.findViewById(R.id.terminalCodeText);
        terminalNameText = view.findViewById(R.id.terminalNameText);
        terminalStatusText = view.findViewById(R.id.terminalStatusText);
        merchantText = view.findViewById(R.id.merchantText);

        MaterialButton logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> viewModel.logout());

        viewModel.user.observe(getViewLifecycleOwner(), this::renderUser);
        viewModel.terminal.observe(getViewLifecycleOwner(), this::renderTerminal);
    }

    private void renderUser(UserEntity user) {
        if (user == null) {
            return;
        }
        greetingText.setText(getString(R.string.home_greeting, user.name));
        userEmailText.setText(user.email);
        userRoleText.setText(user.role);
    }

    private void renderTerminal(TerminalEntity terminal) {
        if (terminal == null) {
            return;
        }
        terminalCodeText.setText(terminal.code);
        terminalNameText.setText(terminal.name);
        terminalStatusText.setText(getString(R.string.home_terminal_status, terminal.status));
        merchantText.setText(getString(R.string.home_merchant_id, terminal.merchantId));
    }
}