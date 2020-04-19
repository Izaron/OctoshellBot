package ru.octoshell.bot.service.statemachine.states.transistor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;
import ru.octoshell.bot.service.statemachine.states.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class StateTransistorServiceImpl implements StateTransistorService {

    private List<State> listenerList;

    public StateTransistorServiceImpl(MainMenuState mainMenuStateListener,
                                      AuthSettingsState authSettingsStateListener,
                                      AuthNewEmailState authNewEmailStateListener,
                                      AuthNewTokenState authNewTokenStateListener,
                                      LocaleSettingsState localeSettingsStateListener,
                                      TicketProjectChooseState ticketProjectChooseStateListener,
                                      TicketTopicChooseState ticketTopicChooseStateListener,
                                      TicketClusterChooseState ticketClusterChooseStateListener,
                                      TicketSubjectChooseState ticketSubjectChooseStateListener,
                                      TicketMessageChooseState ticketMessageChooseStateListener) {

        listenerList = Arrays.asList(
                mainMenuStateListener,
                authSettingsStateListener,
                authNewEmailStateListener,
                authNewTokenStateListener,
                localeSettingsStateListener,
                ticketProjectChooseStateListener,
                ticketTopicChooseStateListener,
                ticketClusterChooseStateListener,
                ticketSubjectChooseStateListener,
                ticketMessageChooseStateListener
        );
    }

    @Override
    public Pair<UserState, Reaction> transition(UserState userState, Update update) {
        State listener = findListenerByUserState(userState);
        if (Objects.nonNull(listener)) {
            Pair<UserState, Reaction> trans = listener.transition(update);
            if (Objects.isNull(trans)) {
                return Pair.of(userState, new Reaction());
            } else if (Objects.isNull(trans.getLeft())) {
                return Pair.of(userState, trans.getRight());
            } else {
                return trans;
            }
        } else {
            log.error("Unknown user state '{}'!", userState);
            return Pair.of(UserState.getDefaultState(), new Reaction());
        }
    }

    @Override
    public Reaction explain(UserState userState, Update update) {
        State listener = findListenerByUserState(userState);
        if (Objects.nonNull(listener)) {
            return listener.explain(update);
        } else {
            log.error("Unknown user state '{}'!", userState);
        }
        return new Reaction();
    }

    private State findListenerByUserState(UserState userState) {
        Class targetClass = userState.getListenerClass();
        for (State state : listenerList) {
            if (state.getClass() == targetClass) {
                return state;
            }
        }
        return null;
    }
}
