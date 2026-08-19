package com.arafath.payterminalversion2.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.TerminalRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsViewModel extends ViewModel {
    private final AuthRepository authRepository;

    public final LiveData<UserEntity> user;
    public final LiveData<MerchantEntity> merchant;
    public final LiveData<TerminalEntity> terminal;

    @Inject
    public SettingsViewModel(
            AuthRepository authRepository,
            TerminalRepository terminalRepository) {
        this.authRepository = authRepository;
        this.user = authRepository.observeUser();
        this.merchant = Transformations.switchMap(user,
                u -> u == null ? null : authRepository.observeMerchant(u.merchantId));
        this.terminal = terminalRepository.observeTerminal();
    }

    public void logout() {
        authRepository.logout();
    }
}