package com.arafath.payterminalversion2.ui.terminal;

import android.os.Bundle;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.arafath.payterminalversion2.util.Time;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TerminalManagementFragment extends Fragment {

    private TerminalManagementViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_terminal_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TerminalManagementViewModel.class);

        view.findViewById(R.id.backButton).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_terminal_to_home));

        ((TextView) view.findViewById(R.id.versionValue)).setText(BuildConfig.VERSION_NAME);
        ((TextView) view.findViewById(R.id.deviceValue)).setText("Android " + Build.VERSION.RELEASE);

        viewModel.user.observe(getViewLifecycleOwner(), this::renderUser);
        viewModel.terminal.observe(getViewLifecycleOwner(), this::renderTerminal);
        viewModel.merchant.observe(getViewLifecycleOwner(), this::renderMerchant);
    }

    private void renderUser(UserEntity user) {
        if (user == null) {
            return;
        }
        if (!user.isOwner()) {
            Toast.makeText(requireContext(), R.string.terminal_owner_only, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigate(R.id.action_terminal_to_home);
        }
    }

    private void renderTerminal(TerminalEntity terminal) {
        if (terminal == null) {
            return;
        }
        View view = requireView();
        ((TextView) view.findViewById(R.id.terminalCodeText)).setText(terminal.code);
        ((TextView) view.findViewById(R.id.terminalNameText)).setText(terminal.name);
        ((TextView) view.findViewById(R.id.terminalStatusValue)).setText(terminal.status);
        String lastSync = terminal.lastHeartbeatAt == null ? "—" : Time.relative(parseMillis(terminal.lastHeartbeatAt));
        ((TextView) view.findViewById(R.id.lastSyncValue)).setText(lastSync);
        String networkStatus;
        if (terminal.lastHeartbeatAt == null) {
            networkStatus = "—";
        } else {
            long diff = System.currentTimeMillis() - parseMillis(terminal.lastHeartbeatAt);
            networkStatus = diff < 2_000_000 ? "Online" : "Offline";
        }
        ((TextView) view.findViewById(R.id.networkStatusValue)).setText(networkStatus);
    }

    private void renderMerchant(MerchantEntity merchant) {
        if (merchant != null) {
            ((TextView) requireView().findViewById(R.id.merchantValue)).setText(merchant.businessName);
        }
    }

    private long parseMillis(String iso) {
        try {
            return java.time.Instant.parse(iso).toEpochMilli();
        } catch (Exception e) {
            return 0L;
        }
    }
}