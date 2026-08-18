package com.arafath.payterminalversion2.data.remote.dto;

public class RegisterTerminalRequest {
    public String merchantId;
    public String pairingCode;
    public String name;

    public RegisterTerminalRequest(String merchantId, String pairingCode, String name) {
        this.merchantId = merchantId;
        this.pairingCode = pairingCode;
        this.name = name;
    }
}