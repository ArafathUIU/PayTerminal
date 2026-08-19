package com.arafath.payterminalversion2.ui.pair;

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
import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PairSuccessFragment extends Fragment {

    private PairSuccessViewModel viewModel;
    private TextView terminalCodeText;
    private TextView merchantNameText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pair_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PairSuccessViewModel.class);

        terminalCodeText = view.findViewById(R.id.terminalCodeText);
        merchantNameText = view.findViewById(R.id.merchantNameText);

        MaterialButton continueButton = view.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v -> {
            NavController nav = Navigation.findNavController(v);
            nav.navigate(R.id.homeFragment);
        });

        viewModel.terminal.observe(getViewLifecycleOwner(), this::renderTerminal);
        viewModel.merchant.observe(getViewLifecycleOwner(), this::renderMerchant);
    }

    private void renderTerminal(TerminalEntity terminal) {
        if (terminal != null) {
            terminalCodeText.setText(terminal.code);
        }
    }

    private void renderMerchant(MerchantEntity merchant) {
        if (merchant != null) {
            merchantNameText.setText(merchant.businessName);
        }
    }
}