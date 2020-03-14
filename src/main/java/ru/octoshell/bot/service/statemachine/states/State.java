package ru.octoshell.bot.service.statemachine.states;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.statemachine.UserState;

/**
 * Интерфейс для текущего состояния пользователя
 */
public interface State {

    /**
     * Переход в другое состояние (возможно, в то же самое)
     * @param bot Движок бота
     * @param update Последние действия пользователя
     * @return
     */
    UserState transition(OctoshellTelegramBot bot, Update update);

    /**
     * Печать состояния: вывод информации и выбор следующего действия
     * @param userState Текущее состояние
     * @param bot Движок бота
     * @param update Последнее сообщение пользователя
     */
    void explain(UserState userState, OctoshellTelegramBot bot, Update update);
}
