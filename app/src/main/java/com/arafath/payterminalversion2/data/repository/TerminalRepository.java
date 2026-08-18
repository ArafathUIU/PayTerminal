package com.arafath.payterminalversion2.data.repository;

import androidx.lifecycle.LiveData;

import com.arafath.payterminalversion2.data.Result;
import com.arafath.payterminalversion2.data.local.dao.TerminalDao;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.remote.ApiErrorParser;
import com.arafath.payterminalversion2.data.remote.api.TerminalApi;
import com.arafath.payterminalversion2.data.remote.dto.RegisterTerminalRequest;
import com.arafath.payterminalversion2.data.remote.dto.TerminalResponse;
import com.arafath.payterminalversion2.di.IoExecutor;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class TerminalRepository {
    private final TerminalApi terminalApi;
    private final TerminalDao terminalDao;
    private final ApiErrorParser errorParser;
    private final Executor ioExecutor;

    @Inject
    public TerminalRepository(
            TerminalApi terminalApi,
            TerminalDao terminalDao,
            ApiErrorParser errorParser,
            @IoExecutor Executor ioExecutor) {
        this.terminalApi = terminalApi;
        this.terminalDao = terminalDao;
        this.errorParser = errorParser;
        this.ioExecutor = ioExecutor;
    }

    public LiveData<TerminalEntity> observeTerminal() {
        return terminalDao.observeFirst();
    }

    public void clear() {
        ioExecutor.execute(terminalDao::deleteAll);
    }

    public void pair(String merchantId, String pairingCode, String name, Consumer<Result<TerminalEntity>> onResult) {
        terminalApi.register(new RegisterTerminalRequest(merchantId, pairingCode, name))
                .enqueue(new Callback<TerminalResponse>() {
                    @Override
                    public void onResponse(Call<TerminalResponse> call, Response<TerminalResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            TerminalEntity entity = toEntity(response.body());
                            ioExecutor.execute(() -> terminalDao.upsert(entity));
                            onResult.accept(Result.ok(entity));
                        } else {
                            onResult.accept(Result.error(errorParser.messageFrom(null, response)));
                        }
                    }

                    @Override
                    public void onFailure(Call<TerminalResponse> call, Throwable t) {
                        onResult.accept(Result.error(errorParser.messageFrom(t, null)));
                    }
                });
    }

    private TerminalEntity toEntity(TerminalResponse dto) {
        TerminalEntity entity = new TerminalEntity();
        entity.id = dto.id;
        entity.merchantId = dto.merchantId;
        entity.code = dto.code;
        entity.name = dto.name;
        entity.status = dto.status;
        entity.pairedAt = dto.pairedAt;
        entity.lastHeartbeatAt = dto.lastHeartbeatAt;
        return entity;
    }
}