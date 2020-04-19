package ru.octoshell.bot.service.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;
import ru.octoshell.bot.service.statemachine.states.transistor.StateTransistorService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class StateMachineEngineService {

    private final UserStateService userStateService;
    private final StateTransistorService stateTransistorService;

    public StateMachineEngineService(UserStateService userStateService,
                                     StateTransistorService stateTransistorService) {
        this.userStateService = userStateService;
        this.stateTransistorService = stateTransistorService;
    }

    public List<Reaction> processUpdate(Update update) {
        if (Objects.isNull(update)) {
            log.info("Wrong update, don't do anything");
            return Collections.emptyList();
        }

        // Change state and draw it
        Integer userId = update.getUserId();
        UserState userState = userStateService.getUserState(userId);

        Pair<UserState, Reaction> trans = stateTransistorService.transition(userState, update);
        Reaction explain = stateTransistorService.explain(trans.getLeft(), update);

        userStateService.setUserState(userId, trans.getLeft());
        return Arrays.asList(trans.getRight(), explain);
    }
}
