package com.arafath.payterminalversion2.ui.session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.TerminalRepository;
import com.arafath.payterminalversion2.data.session.TokenStore;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Drives the app-level session gate: are we logged in, and is a terminal
 * paired? It derives its state from Room (the session mirror) combined with the
 * encrypted token store, so any login/logout/pairing/refresh-failure change is
 * observed reactively by MainActivity.
 */
@HiltViewModel
public class SessionViewModel extends ViewModel {

    public enum State { LOADING, LOGGED_OUT, NEEDS_TERMINAL, READY }

    private final AuthRepository authRepository;
    private final TerminalRepository terminalRepository;

    private final MediatorLiveData<State> state = new MediatorLiveData<>();
    private boolean userLoaded;
    private boolean terminalLoaded;
    private boolean tokenLoaded;

    @Inject
    public SessionViewModel(
            AuthRepository authRepository,
            TerminalRepository terminalRepository,
            TokenStore tokenStore) {
        this.authRepository = authRepository;
        this.terminalRepository = terminalRepository;
        state.setValue(State.LOADING);

        LiveData<UserEntity> user = authRepository.observeUser();
        LiveData<TerminalEntity> terminal = terminalRepository.observeTerminal();
        LiveData<Boolean> tokenActive = tokenStore.sessionActive();

        state.addSource(user, u -> {
            userLoaded = true;
            recomputeState(u, terminal.getValue(), tokenActive.getValue());
        });
        state.addSource(terminal, t -> {
            terminalLoaded = true;
            recomputeState(user.getValue(), t, tokenActive.getValue());
        });
        state.addSource(tokenActive, active -> {
            tokenLoaded = true;
            recomputeState(user.getValue(), terminal.getValue(), active);
        });
    }

    public LiveData<State> getState() {
        return state;
    }

    public void logout() {
        authRepository.logout();
    }

    private void recomputeState(UserEntity user, TerminalEntity terminal, Boolean tokenActive) {
        if (!userLoaded || !terminalLoaded || !tokenLoaded) {
            state.setValue(State.LOADING);
        } else if (tokenActive != Boolean.TRUE || user == null) {
            state.setValue(State.LOGGED_OUT);
        } else if (terminal == null || !terminal.merchantId.equals(user.merchantId)) {
            state.setValue(State.NEEDS_TERMINAL);
        } else {
            state.setValue(State.READY);
        }
    }
}