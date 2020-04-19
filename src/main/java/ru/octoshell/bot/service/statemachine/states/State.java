package ru.octoshell.bot.service.statemachine.states;

import org.apache.commons.lang3.tuple.Pair;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;

/**
 * Интерфейс для текущего состояния пользователя
 */
public interface State {

    /**
     * Переход в другое состояние (возможно, в то же самое)
     * @param update Последние действия пользователя
     */
    Pair<UserState, Reaction> transition(Update update);

    /**
     * Печать состояния: вывод информации и выбор следующего действия
     * @param update Последнее сообщение пользователя
     */
    Reaction explain(Update update);
}
