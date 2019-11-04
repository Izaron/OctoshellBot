package ru.octoshell.bot.service.statemachine.listeners;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.statemachine.UserState;

public interface StateListener {

    UserState processUpdate(UserState userState, OctoshellTelegramBot bot, Update update);

    void drawState(UserState userState, OctoshellTelegramBot bot, Message latestMessage);
}
