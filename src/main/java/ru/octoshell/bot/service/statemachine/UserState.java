package ru.octoshell.bot.service.statemachine;

import ru.octoshell.bot.service.statemachine.states.*;

public enum UserState {
    MAIN_MENU(MainMenuState.class),
    AUTH_SETTINGS(AuthSettingsState.class),
    AUTH_NEW_EMAIL(AuthNewEmailState.class),
    AUTH_NEW_TOKEN(AuthNewTokenState.class),
    LOCALE_SETTINGS(LocaleSettingsState.class),
    TICKET_PROJECT_CHOOSE(TicketProjectChooseState.class),
    TICKET_TOPIC_CHOOSE(TicketTopicChooseState.class),
    TICKET_CLUSTER_CHOOSE(TicketClusterChooseState.class),
    TICKET_SUBJECT_CHOOSE(TicketSubjectChooseState.class),
    TICKET_MESSAGE_CHOOSE(TicketMessageChooseState.class);

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
