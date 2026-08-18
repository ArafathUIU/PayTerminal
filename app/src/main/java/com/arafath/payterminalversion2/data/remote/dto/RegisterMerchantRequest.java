package com.arafath.payterminalversion2.data.remote.dto;

public class RegisterMerchantRequest {
    public String name;
    public String businessName;
    public String email;
    public String password;
    public String phone;

    public RegisterMerchantRequest(String name, String businessName, String email, String password, String phone) {
        this.name = name;
        this.businessName = businessName;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }
}