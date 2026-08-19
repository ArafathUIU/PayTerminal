package com.arafath.payterminalversion2.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;
import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.PaymentRepository;
import com.arafath.payterminalversion2.data.repository.TerminalRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private final AuthRepository authRepository;

    public final LiveData<UserEntity> user;
    public final LiveData<TerminalEntity> terminal;
    public final LiveData<MerchantEntity> merchant;

    public final LiveData<Long> todayTotal;
    public final LiveData<Integer> transactionCount;
    public final LiveData<Integer> successfulCount;
    public final LiveData<Integer> failedCount;
    public final LiveData<List<PaymentTransactionEntity>> recent;

    @Inject
    public HomeViewModel(
            AuthRepository authRepository,
            TerminalRepository terminalRepository,
            PaymentRepository paymentRepository) {
        this.authRepository = authRepository;
        this.user = authRepository.observeUser();
        this.terminal = terminalRepository.observeTerminal();
        this.merchant = Transformations.switchMap(user,
                u -> u == null ? null : authRepository.observeMerchant(u.merchantId));

        LiveData<String> merchantId = Transformations.map(user,
                u -> u == null ? null : u.merchantId);

        this.todayTotal = Transformations.switchMap(merchantId,
                m -> m == null ? null : paymentRepository.observeTodayTotal(m));
        this.transactionCount = Transformations.switchMap(merchantId,
                m -> m == null ? null : paymentRepository.observeTransactionCount(m));
        this.successfulCount = Transformations.switchMap(merchantId,
                m -> m == null ? null : paymentRepository.observeSuccessfulCount(m));
        this.failedCount = Transformations.switchMap(merchantId,
                m -> m == null ? null : paymentRepository.observeFailedCount(m));
        this.recent = Transformations.switchMap(merchantId,
                m -> m == null ? null : paymentRepository.observeRecent(m, 6));
    }

    public void logout() {
        authRepository.logout();
    }
}