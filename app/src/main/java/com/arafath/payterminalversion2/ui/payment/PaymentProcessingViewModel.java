package com.arafath.payterminalversion2.ui.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
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
public class PaymentProcessingViewModel extends ViewModel {
    private final PaymentRepository paymentRepository;
    private final AuthRepository authRepository;
    private final TerminalRepository terminalRepository;

    private final MutableLiveData<PaymentTransactionEntity> result = new MutableLiveData<>();

    @Inject
    public PaymentProcessingViewModel(
            PaymentRepository paymentRepository,
            AuthRepository authRepository,
            TerminalRepository terminalRepository) {
        this.paymentRepository = paymentRepository;
        this.authRepository = authRepository;
        this.terminalRepository = terminalRepository;
    }

    public LiveData<PaymentTransactionEntity> result() {
        return result;
    }

    public void process(long amountPaise, String method, String maskedRef) {
        UserEntity user = authRepository.observeUser().getValue();
        TerminalEntity terminal = terminalRepository.observeTerminal().getValue();
        if (user == null || terminal == null) {
            return;
        }
        boolean shouldFail = amountPaise % 100 == 99; // demo rule: amounts ending in .99 decline
        paymentRepository.process(amountPaise, method, maskedRef, user, terminal, shouldFail, result::postValue);
    }
}