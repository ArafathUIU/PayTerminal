package com.arafath.payterminalversion2.ui.pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.TerminalRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PairSuccessViewModel extends ViewModel {
    public final LiveData<TerminalEntity> terminal;
    public final LiveData<MerchantEntity> merchant;

    @Inject
    public PairSuccessViewModel(
            AuthRepository authRepository,
            TerminalRepository terminalRepository) {
        LiveData<UserEntity> user = authRepository.observeUser();
        this.terminal = terminalRepository.observeTerminal();
        this.merchant = androidx.lifecycle.Transformations.switchMap(user,
                u -> u == null ? null : authRepository.observeMerchant(u.merchantId));
    }
}