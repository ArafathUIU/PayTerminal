package com.arafath.payterminalversion2.ui.transactions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.arafath.payterminalversion2.R;
import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;
import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.PaymentRepository;
import com.arafath.payterminalversion2.util.Money;
import com.arafath.payterminalversion2.util.Time;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DigitalReceiptFragment extends Fragment {

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    AuthRepository authRepository;

    private PaymentTransactionEntity transaction;
    private String merchantName = "";
    private String fileName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_digital_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String txId = DigitalReceiptFragmentArgs.fromBundle(requireArguments()).getTransactionId();
        transaction = paymentRepository.getById(txId);

        authRepository.observeUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                return;
            }
            authRepository.observeMerchant(user.merchantId)
                    .observe(getViewLifecycleOwner(), merchant -> {
                        if (merchant != null) {
                            merchantName = merchant.businessName;
                            renderMerchantName();
                        }
                    });
        });

        renderReceipt();
        renderMerchantName();

        view.findViewById(R.id.backButton).setOnClickListener(v -> requireActivity().onBackPressed());

        MaterialButton saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveReceipt());

        MaterialButton shareButton = view.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(v -> shareReceipt());
    }

    private void renderMerchantName() {
        TextView merchantText = requireView().findViewById(R.id.merchantNameText);
        merchantText.setText(merchantName.isEmpty() ? "Merchant" : merchantName);
    }

    private void renderReceipt() {
        View view = requireView();
        ((TextView) view.findViewById(R.id.terminalCodeText)).setText(transaction.terminalCode);
        ((TextView) view.findViewById(R.id.transactionIdText)).setText(transaction.id);
        ((TextView) view.findViewById(R.id.dateTimeText)).setText(Time.dateTime(transaction.createdAt));
        ((TextView) view.findViewById(R.id.methodText)).setText(transaction.method);
        ((TextView) view.findViewById(R.id.referenceText)).setText(transaction.cardMasked + " · " + transaction.reference);
        ((TextView) view.findViewById(R.id.amountText)).setText(Money.format(transaction.amountPaise));
        ((TextView) view.findViewById(R.id.statusText)).setText(
                PaymentTransactionEntity.STATUS_SUCCESS.equals(transaction.status) ? "PAID" : transaction.status);
    }

    private String receiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("PayTerminal\n");
        sb.append(merchantName).append('\n');
        sb.append(transaction.terminalCode).append('\n');
        sb.append("--------------------------------\n");
        sb.append("Transaction ID: ").append(transaction.id).append('\n');
        sb.append("Date: ").append(Time.dateTime(transaction.createdAt)).append('\n');
        sb.append("--------------------------------\n");
        sb.append("Method: ").append(transaction.method).append('\n');
        sb.append("Reference: ").append(transaction.cardMasked).append('\n');
        sb.append("--------------------------------\n");
        sb.append("Total: ").append(Money.format(transaction.amountPaise)).append('\n');
        sb.append("Status: ").append(transaction.status).append('\n');
        sb.append("--------------------------------\n");
        sb.append("Thank you\n");
        return sb.toString();
    }

    private void saveReceipt() {
        fileName = "receipt_" + transaction.id + ".txt";
        File file = new File(requireContext().getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(receiptText().getBytes(StandardCharsets.UTF_8));
            Toast.makeText(requireContext(), "Receipt saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Could not save receipt", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReceipt() {
        saveReceipt();
        File file = new File(requireContext().getCacheDir(), fileName);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "PayTerminal Receipt " + transaction.id);
        intent.putExtra(Intent.EXTRA_TEXT, receiptText());
        try {
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (IllegalArgumentException ignored) {
            // FileProvider not configured; share as plain text.
        }
        startActivity(Intent.createChooser(intent, "Share receipt"));
    }
}