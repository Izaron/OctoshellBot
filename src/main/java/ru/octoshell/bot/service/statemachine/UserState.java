package ru.octoshell.bot.service.statemachine;

import ru.octoshell.bot.service.statemachine.listeners.AuthNewEmailStateListener;
import ru.octoshell.bot.service.statemachine.listeners.AuthNewTokenStateListener;
import ru.octoshell.bot.service.statemachine.listeners.AuthSettingsStateListener;
import ru.octoshell.bot.service.statemachine.listeners.MainMenuStateListener;

public enum UserState {
    MAIN_MENU(MainMenuStateListener.class),
    AUTH_SETTINGS(AuthSettingsStateListener.class),
    AUTH_NEW_EMAIL(AuthNewEmailStateListener.class),
    AUTH_NEW_TOKEN(AuthNewTokenStateListener.class);

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
