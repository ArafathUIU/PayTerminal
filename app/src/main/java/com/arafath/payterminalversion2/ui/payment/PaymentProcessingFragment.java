package com.arafath.payterminalversion2.ui.payment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.util.Money;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PaymentProcessingFragment extends Fragment {

    private long amountPaise;
    private String method;
    private String maskedRef;

    private View step1Dot;
    private View step2Dot;
    private View step3Dot;
    private TextView step1Status;
    private TextView step2Status;
    private TextView step3Status;
    private View timelineLine2;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_processing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PaymentProcessingFragmentArgs args = PaymentProcessingFragmentArgs.fromBundle(requireArguments());
        amountPaise = args.getAmountPaise();
        method = args.getMethod();
        maskedRef = args.getMaskedRef();

        ((TextView) view.findViewById(R.id.amountText)).setText(Money.format(amountPaise));
        ((TextView) view.findViewById(R.id.methodText)).setText(method + " · " + maskedRef);

        step1Dot = view.findViewById(R.id.step1Dot);
        step2Dot = view.findViewById(R.id.step2Dot);
        step3Dot = view.findViewById(R.id.step3Dot);
        step1Status = view.findViewById(R.id.step1Status);
        step2Status = view.findViewById(R.id.step2Status);
        step3Status = view.findViewById(R.id.step3Status);
        timelineLine2 = view.findViewById(R.id.timelineLine2);

        runSimulation();
    }

    private void runSimulation() {
        PaymentProcessingViewModel viewModel = new ViewModelProvider(this).get(PaymentProcessingViewModel.class);
        step1Status.setText("done");

        handler.postDelayed(() -> {
            step2Status.setText("done");
            markDone(step2Dot);
        }, 450);

        handler.postDelayed(() -> {
            step3Status.setText("processing…");
            step3Dot.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_dot_online));
            timelineLine2.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange));
        }, 900);

        viewModel.result().observe(getViewLifecycleOwner(), this::onResult);
        viewModel.process(amountPaise, method, maskedRef);
    }

    private void markDone(View dot) {
        dot.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_dot_online));
    }

    private void onResult(PaymentTransactionEntity tx) {
        step3Status.setText("done");
        markDone(step3Dot);
        NavController nav = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putString("transactionId", tx.id);
        int action = PaymentTransactionEntity.STATUS_SUCCESS.equals(tx.status)
                ? R.id.action_processing_to_success
                : R.id.action_processing_to_failed;
        nav.navigate(action, args);
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}