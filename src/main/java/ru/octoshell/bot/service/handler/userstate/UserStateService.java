package ru.octoshell.bot.service.handler.userstate;

import ru.octoshell.bot.service.statemachine.UserState;

public interface UserStateService {
    UserState getUserState(Integer userId);

    void setUserState(Integer userId, UserState userState);

    String getUserLocale(Integer userId);

    void setUserLocale(Integer userId, String locale);
}
