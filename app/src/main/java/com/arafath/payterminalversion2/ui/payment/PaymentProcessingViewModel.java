package com.arafath.payterminalversion2.ui.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
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
    private boolean started;
    private boolean launched;

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
        if (started) {
            return;
        }
        started = true;
        LiveData<UserEntity> userLiveData = authRepository.observeUser();
        LiveData<TerminalEntity> terminalLiveData = terminalRepository.observeTerminal();

        Observer<UserEntity> userObserver = user -> {
            TerminalEntity terminal = terminalLiveData.getValue();
            if (user != null && terminal != null) {
                start(amountPaise, method, maskedRef, user, terminal);
            }
        };
        Observer<TerminalEntity> terminalObserver = terminal -> {
            UserEntity user = userLiveData.getValue();
            if (user != null && terminal != null) {
                start(amountPaise, method, maskedRef, user, terminal);
            }
        };

        userLiveData.observeForever(userObserver);
        terminalLiveData.observeForever(terminalObserver);

        UserEntity user = userLiveData.getValue();
        TerminalEntity terminal = terminalLiveData.getValue();
        if (user != null && terminal != null) {
            userLiveData.removeObserver(userObserver);
            terminalLiveData.removeObserver(terminalObserver);
            start(amountPaise, method, maskedRef, user, terminal);
        }
    }

    private void start(long amountPaise, String method, String maskedRef, UserEntity user, TerminalEntity terminal) {
        if (launched) {
            return;
        }
        launched = true;
        boolean shouldFail = amountPaise % 100 == 99; // demo rule: amounts ending in .99 decline
        paymentRepository.process(amountPaise, method, maskedRef, user, terminal, shouldFail, result::postValue);
    }
}