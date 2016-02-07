package com.lach.translink.network.gocard;

public interface GoCardCredentials {
    String getCardNumber();

    String getPassword();

    boolean credentialsExist();

    void update(String cardNumber, String password);

    void clear();
}
