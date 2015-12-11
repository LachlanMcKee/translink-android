package com.lach.translink.network;

public interface GoCardCredentials {
    String getCardNumber();

    String getPassword();

    boolean credentialsExist();

    void update(String cardNumber, String password);

    void clear();
}
