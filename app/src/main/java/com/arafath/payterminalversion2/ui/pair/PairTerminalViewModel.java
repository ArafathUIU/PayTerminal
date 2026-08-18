package com.arafath.payterminalversion2.ui.pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.TerminalRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PairTerminalViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final TerminalRepository terminalRepository;

    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> error = new MutableLiveData<>();
    private String merchantId;
    private final androidx.lifecycle.Observer<UserEntity> userObserver =
            user -> merchantId = user != null ? user.merchantId : null;

    @Inject
    public PairTerminalViewModel(AuthRepository authRepository, TerminalRepository terminalRepository) {
        this.authRepository = authRepository;
        this.terminalRepository = terminalRepository;
        authRepository.observeUser().observeForever(userObserver);
    }

    @Override
    protected void onCleared() {
        authRepository.observeUser().removeObserver(userObserver);
        super.onCleared();
    }

    public void pair(String pairingCode, String terminalName) {
        String code = pairingCode == null ? "" : pairingCode.trim();
        String name = terminalName == null ? "" : terminalName.trim();

        if (code.isEmpty() || name.isEmpty()) {
            error.setValue("Enter the pairing code and a terminal name");
            return;
        }
        if (merchantId == null) {
            error.setValue("No merchant on this session. Log out and log in again.");
            return;
        }

        loading.setValue(true);
        terminalRepository.pair(merchantId, code, name, result -> {
            loading.postValue(false);
            if (!result.success) {
                error.postValue(result.errorMessage);
            }
        });
    }
}