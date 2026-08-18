package com.arafath.payterminalversion2.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.TerminalRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private final AuthRepository authRepository;

    public final LiveData<UserEntity> user;
    public final LiveData<TerminalEntity> terminal;

    @Inject
    public HomeViewModel(AuthRepository authRepository, TerminalRepository terminalRepository) {
        this.authRepository = authRepository;
        this.user = authRepository.observeUser();
        this.terminal = terminalRepository.observeTerminal();
    }

    public void logout() {
        authRepository.logout();
    }
}