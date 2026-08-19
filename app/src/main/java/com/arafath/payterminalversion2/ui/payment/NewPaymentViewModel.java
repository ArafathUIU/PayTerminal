package com.arafath.payterminalversion2.ui.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.repository.TerminalRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NewPaymentViewModel extends ViewModel {
    private final MutableLiveData<String> amount = new MutableLiveData<>("");
    private final TerminalRepository terminalRepository;

    public final LiveData<Long> amountPaise;
    public final LiveData<String> terminalLabel;

    @Inject
    public NewPaymentViewModel(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
        this.amountPaise = Transformations.map(amount, s -> {
            if (s == null || s.isEmpty()) {
                return 0L;
            }
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return 0L;
            }
        });
        this.terminalLabel = Transformations.map(terminalRepository.observeTerminal(),
                t -> t == null ? null : t.code + " · Online");
    }

    public void appendDigit(String digit) {
        String current = amount.getValue() == null ? "" : amount.getValue();
        if (current.length() >= 9) {
            return;
        }
        if (current.equals("0")) {
            current = "";
        }
        amount.setValue(current + digit);
    }

    public void backspace() {
        String current = amount.getValue() == null ? "" : amount.getValue();
        if (!current.isEmpty()) {
            amount.setValue(current.substring(0, current.length() - 1));
        }
    }

    public void clear() {
        amount.setValue("");
    }

    public long amountPaise() {
        Long value = amountPaise.getValue();
        return value == null ? 0L : value;
    }
}