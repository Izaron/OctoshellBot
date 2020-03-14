package ru.octoshell.bot.service.statemachine.states.transistor;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.statemachine.UserState;

public interface StateTransistorService {
    UserState transition(UserState userState, OctoshellTelegramBot bot, Update update);

    void explain(UserState userState, OctoshellTelegramBot bot, Update update);
}
