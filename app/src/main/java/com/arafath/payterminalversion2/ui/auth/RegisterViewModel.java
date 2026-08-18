package com.arafath.payterminalversion2.ui.auth;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterViewModel extends ViewModel {
    private final AuthRepository authRepository;

    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public RegisterViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void register(String name, String businessName, String email, String password, String phone) {
        String cleanName = name == null ? "" : name.trim();
        String cleanBusiness = businessName == null ? "" : businessName.trim();
        String cleanEmail = email == null ? "" : email.trim();
        String cleanPassword = password == null ? "" : password;

        if (cleanName.isEmpty() || cleanBusiness.isEmpty() || cleanEmail.isEmpty()) {
            error.setValue("Name, business name and email are required");
            return;
        }
        if (cleanPassword.length() < 8) {
            error.setValue("Password must be at least 8 characters");
            return;
        }

        loading.setValue(true);
        authRepository.register(
                cleanName,
                cleanBusiness,
                cleanEmail,
                cleanPassword,
                phone == null ? null : phone.trim(),
                result -> {
                    loading.postValue(false);
                    if (!result.success) {
                        error.postValue(result.errorMessage);
                    }
                });
    }
}