package com.arafath.payterminalversion2.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;
import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.util.Money;
import com.arafath.payterminalversion2.util.Time;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    private TextView greetingText;
    private TextView merchantNameText;
    private TextView terminalCodeText;
    private TextView terminalStatusText;
    private TextView todayTotalText;
    private TextView transactionCountText;
    private TextView successfulCountText;
    private TextView failedCountText;
    private LinearLayout recentList;
    private TextView emptyRecentText;

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
        merchantNameText = view.findViewById(R.id.merchantNameText);
        terminalCodeText = view.findViewById(R.id.terminalCodeText);
        terminalStatusText = view.findViewById(R.id.terminalStatusText);
        todayTotalText = view.findViewById(R.id.todayTotalText);
        transactionCountText = view.findViewById(R.id.transactionCountText);
        successfulCountText = view.findViewById(R.id.successfulCountText);
        failedCountText = view.findViewById(R.id.failedCountText);
        recentList = view.findViewById(R.id.recentList);
        emptyRecentText = view.findViewById(R.id.emptyRecentText);

        view.findViewById(R.id.newPaymentButton).setOnClickListener(v -> navigate(R.id.action_home_to_new_payment));
        view.findViewById(R.id.transactionsButton).setOnClickListener(v -> navigate(R.id.action_home_to_transactions));
        view.findViewById(R.id.terminalButton).setOnClickListener(v -> navigate(R.id.action_home_to_terminal));
        view.findViewById(R.id.settingsButton).setOnClickListener(v -> navigate(R.id.action_home_to_settings));

        viewModel.user.observe(getViewLifecycleOwner(), this::renderUser);
        viewModel.merchant.observe(getViewLifecycleOwner(), this::renderMerchant);
        viewModel.terminal.observe(getViewLifecycleOwner(), this::renderTerminal);
        viewModel.todayTotal.observe(getViewLifecycleOwner(), v -> todayTotalText.setText(Money.format(v)));
        viewModel.transactionCount.observe(getViewLifecycleOwner(), v -> transactionCountText.setText(String.valueOf(v)));
        viewModel.successfulCount.observe(getViewLifecycleOwner(), v -> successfulCountText.setText(String.valueOf(v)));
        viewModel.failedCount.observe(getViewLifecycleOwner(), v -> failedCountText.setText(String.valueOf(v)));
        viewModel.recent.observe(getViewLifecycleOwner(), this::renderRecent);
    }

    private void navigate(int actionId) {
        NavController nav = Navigation.findNavController(requireView());
        if (nav.getCurrentDestination() != null && nav.getCurrentDestination().getId() == R.id.homeFragment) {
            nav.navigate(actionId);
        }
    }

    private void renderUser(UserEntity user) {
        if (user != null) {
            greetingText.setText(getString(R.string.home_greeting, user.name));
        }
    }

    private void renderMerchant(MerchantEntity merchant) {
        if (merchant != null) {
            merchantNameText.setText(merchant.businessName);
        }
    }

    private void renderTerminal(TerminalEntity terminal) {
        if (terminal == null) {
            return;
        }
        terminalCodeText.setText(terminal.code);
        terminalStatusText.setText(terminal.status);
    }

    private void renderRecent(List<PaymentTransactionEntity> transactions) {
        recentList.removeAllViews();
        if (transactions == null || transactions.isEmpty()) {
            emptyRecentText.setVisibility(View.VISIBLE);
            return;
        }
        emptyRecentText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (PaymentTransactionEntity tx : transactions) {
            View row = inflater.inflate(R.layout.item_recent_transaction, recentList, false);
            ((TextView) row.findViewById(R.id.txIdText)).setText(tx.id);
            ((TextView) row.findViewById(R.id.txAmountText)).setText(Money.format(tx.amountPaise));
            ((TextView) row.findViewById(R.id.txMetaText))
                    .setText(tx.method + " · " + Time.relative(tx.createdAt));
            View dot = row.findViewById(R.id.statusDot);
            dot.setBackground(ContextCompat.getDrawable(requireContext(), statusDot(tx.status)));
            recentList.addView(row);
        }
    }

    private int statusDot(String status) {
        if (PaymentTransactionEntity.STATUS_SUCCESS.equals(status)) {
            return R.drawable.ic_dot_online;
        }
        return R.drawable.ic_dot_error;
    }
}