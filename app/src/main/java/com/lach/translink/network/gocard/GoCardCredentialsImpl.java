package com.lach.translink.network.gocard;

import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.data.preference.StringPreference;

import javax.inject.Inject;

public class GoCardCredentialsImpl implements GoCardCredentials {
    private static final StringPreference PREF_CARD_NUMBER = new StringPreference("cardNum", "");
    private static final StringPreference PREF_PASSWORD = new StringPreference("cardPass", "");

    private final PreferencesProvider preferencesProvider;

    @Inject
    public GoCardCredentialsImpl(PreferencesProvider preferencesProvider) {
        this.preferencesProvider = preferencesProvider;
    }

    @Override
    public boolean credentialsExist() {
        Preferences preferences = preferencesProvider.getPreferences();
        return PREF_CARD_NUMBER.exists(preferences) && PREF_PASSWORD.exists(preferences);
    }

    @Override
    public void update(String cardNumber, String password) {
        Preferences.Editor editor = preferencesProvider.getPreferences().edit();
        PREF_CARD_NUMBER.set(editor, cardNumber);

        if (password.length() > 0) {
            PREF_PASSWORD.set(editor, password);
        }
        editor.apply();
    }

    @Override
    public void clear() {
        Preferences preferences = preferencesProvider.getPreferences();
        Preferences.Editor editor = preferences.edit();
        PREF_CARD_NUMBER.remove(editor);
        PREF_PASSWORD.remove(editor);
        editor.apply();
    }

    @Override
    public String getCardNumber() {
        Preferences preferences = preferencesProvider.getPreferences();
        return PREF_CARD_NUMBER.get(preferences);
    }

    @Override
    public String getPassword() {
        Preferences preferences = preferencesProvider.getPreferences();
        return PREF_PASSWORD.get(preferences);
    }
}
