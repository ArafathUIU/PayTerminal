package com.arafath.payterminalversion2.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.arafath.payterminalversion2.BuildConfig;
import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        view.findViewById(R.id.backButton).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_settings_to_home));

        ((TextView) view.findViewById(R.id.versionText)).setText(BuildConfig.VERSION_NAME);

        MaterialButton logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> viewModel.logout());

        viewModel.user.observe(getViewLifecycleOwner(), this::renderUser);
        viewModel.merchant.observe(getViewLifecycleOwner(), this::renderMerchant);
        viewModel.terminal.observe(getViewLifecycleOwner(), this::renderTerminal);
    }

    private void renderUser(UserEntity user) {
        if (user != null) {
            ((TextView) requireView().findViewById(R.id.profileNameText)).setText(user.name);
            ((TextView) requireView().findViewById(R.id.profileEmailText)).setText(user.email);
        }
    }

    private void renderMerchant(MerchantEntity merchant) {
        if (merchant != null) {
            ((TextView) requireView().findViewById(R.id.profileBusinessText)).setText(merchant.businessName);
        }
    }

    private void renderTerminal(TerminalEntity terminal) {
        if (terminal != null) {
            ((TextView) requireView().findViewById(R.id.settingsTerminalCodeText)).setText(terminal.code);
            ((TextView) requireView().findViewById(R.id.settingsTerminalStatusText)).setText(terminal.status);
        }
    }
}