package com.arafath.payterminalversion2.ui.auth;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.remote.dto.AuthResponse;
import com.arafath.payterminalversion2.data.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final AuthRepository authRepository;

    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void login(String email, String password) {
        String cleanEmail = email == null ? "" : email.trim();
        String cleanPassword = password == null ? "" : password;

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            error.setValue("Enter your email and password");
            return;
        }

        loading.setValue(true);
        authRepository.login(cleanEmail, cleanPassword, result -> {
            loading.postValue(false);
            if (!result.success) {
                error.postValue(result.errorMessage);
            }
        });
    }
}