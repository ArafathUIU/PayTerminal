package com.arafath.payterminalversion2.ui.refund;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.PaymentRepository;
import com.arafath.payterminalversion2.data.repository.TerminalRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RefundViewModel extends ViewModel {
    private final PaymentRepository paymentRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Long> originalAmount = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> refunded = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    private PaymentTransactionEntity transaction;

    @Inject
    public RefundViewModel(
            PaymentRepository paymentRepository,
            AuthRepository authRepository) {
        this.paymentRepository = paymentRepository;
        this.authRepository = authRepository;
    }

    public LiveData<Long> originalAmount() {
        return originalAmount;
    }

    public LiveData<Boolean> refunded() {
        return refunded;
    }

    public LiveData<String> error() {
        return error;
    }

    public void load(String transactionId) {
        transaction = paymentRepository.getById(transactionId);
        if (transaction != null) {
            originalAmount.setValue(transaction.amountPaise);
        }
    }

    public void confirm(long refundPaise, String reason) {
        if (transaction == null) {
            error.setValue("Transaction not found");
            return;
        }
        if (refundPaise <= 0 || refundPaise > transaction.amountPaise) {
            error.setValue("Refund amount must be between 1 and the original amount");
            return;
        }
        UserEntity user = authRepository.observeUser().getValue();
        if (user == null) {
            error.setValue("Not signed in");
            return;
        }
        refunded.setValue(false);
        error.setValue(null);
        paymentRepository.refund(transaction, refundPaise, reason, user, result -> {
            transaction = result;
            refunded.postValue(true);
        });
    }
}