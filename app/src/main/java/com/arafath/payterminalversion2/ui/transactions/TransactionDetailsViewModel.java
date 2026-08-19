package com.arafath.payterminalversion2.ui.transactions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.PaymentRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransactionDetailsViewModel extends ViewModel {
    private final PaymentRepository paymentRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<PaymentTransactionEntity> transaction = new MutableLiveData<>();

    @Inject
    public TransactionDetailsViewModel(
            PaymentRepository paymentRepository,
            AuthRepository authRepository) {
        this.paymentRepository = paymentRepository;
        this.authRepository = authRepository;
    }

    public LiveData<PaymentTransactionEntity> transaction() {
        return transaction;
    }

    public LiveData<com.arafath.payterminalversion2.data.local.entity.MerchantEntity> merchant() {
        return androidx.lifecycle.Transformations.switchMap(authRepository.observeUser(),
                u -> u == null ? null : authRepository.observeMerchant(u.merchantId));
    }

    public void load(String transactionId) {
        PaymentTransactionEntity tx = paymentRepository.getById(transactionId);
        transaction.setValue(tx);
    }
}