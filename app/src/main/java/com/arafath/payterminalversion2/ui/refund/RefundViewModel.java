package com.arafath.payterminalversion2.ui.refund;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.PaymentRepository;

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
    private LiveData<PaymentTransactionEntity> txLiveData;
    private final Observer<PaymentTransactionEntity> txObserver = tx -> {
        transaction = tx;
        if (tx != null) {
            originalAmount.setValue(tx.amountPaise);
        }
    };

    private UserEntity user;
    private LiveData<UserEntity> userLiveData;
    private final Observer<UserEntity> userObserver = u -> user = u;

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
        if (txLiveData != null) {
            txLiveData.removeObserver(txObserver);
        }
        txLiveData = paymentRepository.observeById(transactionId);
        txLiveData.observeForever(txObserver);
        if (userLiveData == null) {
            userLiveData = authRepository.observeUser();
            userLiveData.observeForever(userObserver);
        }
    }

    @Override
    protected void onCleared() {
        if (txLiveData != null) {
            txLiveData.removeObserver(txObserver);
        }
        if (userLiveData != null) {
            userLiveData.removeObserver(userObserver);
        }
        super.onCleared();
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