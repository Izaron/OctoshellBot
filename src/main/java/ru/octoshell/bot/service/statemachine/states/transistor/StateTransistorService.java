package ru.octoshell.bot.service.statemachine.states.transistor;

import org.apache.commons.lang3.tuple.Pair;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;

public interface StateTransistorService {

    Pair<UserState, Reaction> transition(UserState userState, Update update);
    Reaction explain(UserState userState, Update update);
}
