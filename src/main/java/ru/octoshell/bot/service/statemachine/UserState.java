package ru.octoshell.bot.service.statemachine;

import ru.octoshell.bot.service.statemachine.listeners.*;

public enum UserState {
    MAIN_MENU(MainMenuStateListener.class),
    AUTH_SETTINGS(AuthSettingsStateListener.class),
    AUTH_NEW_EMAIL(AuthNewEmailStateListener.class),
    AUTH_NEW_TOKEN(AuthNewTokenStateListener.class),
    LOCALE_SETTINGS(LocaleSettingsStateListener.class);

    private final Class listenerClass;

    UserState(Class listenerClass) {
        this.listenerClass = listenerClass;
    }

    public static UserState getDefaultState() {
        return MAIN_MENU;
    }

    public Class getListenerClass() {
        return listenerClass;
    }
}
