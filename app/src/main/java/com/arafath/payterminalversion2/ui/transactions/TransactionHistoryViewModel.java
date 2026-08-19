package com.arafath.payterminalversion2.ui.transactions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.repository.AuthRepository;
import com.arafath.payterminalversion2.data.repository.PaymentRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransactionHistoryViewModel extends ViewModel {
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<String> methodFilter = new MutableLiveData<>("ALL");
    private final MutableLiveData<String> statusFilter = new MutableLiveData<>("ALL");

    private final MediatorLiveData<List<PaymentTransactionEntity>> transactions = new MediatorLiveData<>();
    private LiveData<List<PaymentTransactionEntity>> allTransactions;

    @Inject
    public TransactionHistoryViewModel(
            AuthRepository authRepository,
            PaymentRepository paymentRepository) {
        LiveData<String> merchantId = Transformations.map(authRepository.observeUser(),
                u -> u == null ? null : u.merchantId);
        allTransactions = Transformations.switchMap(merchantId,
                m -> m == null ? null : paymentRepository.observeAll(m));

        transactions.addSource(allTransactions, list -> refresh());
        transactions.addSource(query, q -> refresh());
        transactions.addSource(methodFilter, f -> refresh());
        transactions.addSource(statusFilter, f -> refresh());
    }

    public LiveData<List<PaymentTransactionEntity>> transactions() {
        return transactions;
    }

    public void setQuery(String q) {
        query.setValue(q == null ? "" : q);
    }

    public void setMethodFilter(String f) {
        methodFilter.setValue(f);
    }

    public void setStatusFilter(String f) {
        statusFilter.setValue(f);
    }

    private void refresh() {
        List<PaymentTransactionEntity> all = allTransactions.getValue();
        if (all == null) {
            transactions.setValue(null);
            return;
        }
        String needle = query.getValue() == null ? "" : query.getValue().toLowerCase().trim();
        String method = methodFilter.getValue() == null ? "ALL" : methodFilter.getValue();
        String status = statusFilter.getValue() == null ? "ALL" : statusFilter.getValue();

        List<PaymentTransactionEntity> result = new ArrayList<>();
        for (PaymentTransactionEntity tx : all) {
            if (!"ALL".equals(method) && !method.equals(tx.method)) {
                continue;
            }
            if (!"ALL".equals(status) && !status.equals(tx.status)) {
                continue;
            }
            if (!needle.isEmpty()
                    && !tx.id.toLowerCase().contains(needle)
                    && !tx.reference.toLowerCase().contains(needle)) {
                continue;
            }
            result.add(tx);
        }
        transactions.setValue(result);
    }
}