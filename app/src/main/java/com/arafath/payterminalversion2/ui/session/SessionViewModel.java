package com.arafath.payterminalversion2.ui.session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.TerminalRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Drives the app-level session gate: are we logged in, and is a terminal
 * paired? It derives its state from Room, which is the single source of truth
 * for the session mirror, so any login/logout/pairing change is observed
 * reactively by MainActivity.
 */
@HiltViewModel
public class SessionViewModel extends ViewModel {

    public enum State { LOADING, LOGGED_OUT, NEEDS_TERMINAL, READY }

    private final AuthRepository authRepository;
    private final TerminalRepository terminalRepository;

    private final MediatorLiveData<State> state = new MediatorLiveData<>();
    private boolean userLoaded;
    private boolean terminalLoaded;

    @Inject
    public SessionViewModel(AuthRepository authRepository, TerminalRepository terminalRepository) {
        this.authRepository = authRepository;
        this.terminalRepository = terminalRepository;
        state.setValue(State.LOADING);

        LiveData<UserEntity> user = authRepository.observeUser();
        LiveData<TerminalEntity> terminal = terminalRepository.observeTerminal();

        state.addSource(user, u -> {
            userLoaded = true;
            recomputeState(u, terminal.getValue());
        });
        state.addSource(terminal, t -> {
            terminalLoaded = true;
            recomputeState(user.getValue(), t);
        });
    }

    public LiveData<State> getState() {
        return state;
    }

    public void logout() {
        authRepository.logout();
        terminalRepository.clear();
    }

    private void recomputeState(UserEntity user, TerminalEntity terminal) {
        if (!userLoaded || !terminalLoaded) {
            state.setValue(State.LOADING);
        } else if (user == null) {
            state.setValue(State.LOGGED_OUT);
        } else if (terminal == null || !terminal.merchantId.equals(user.merchantId)) {
            state.setValue(State.NEEDS_TERMINAL);
        } else {
            state.setValue(State.READY);
        }
    }
}