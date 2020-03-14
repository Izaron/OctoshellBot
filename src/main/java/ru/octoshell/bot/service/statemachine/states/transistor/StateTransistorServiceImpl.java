package ru.octoshell.bot.service.statemachine.states.transistor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.statemachine.UserState;
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
    public UserState transition(UserState userState, OctoshellTelegramBot bot, Update update) {
        State listener = findListenerByUserState(userState);
        if (Objects.nonNull(listener)) {
            UserState newState = listener.transition(bot, update);
            return Objects.isNull(newState) ? userState : newState;
        } else {
            log.error("Unknown user state '{}'!", userState);
            return UserState.getDefaultState();
        }
    }

    @Override
    public void explain(UserState userState, OctoshellTelegramBot bot, Update update) {
        State listener = findListenerByUserState(userState);
        if (Objects.nonNull(listener)) {
            listener.explain(userState, bot, update);
        } else {
            log.error("Unknown user state '{}'!", userState);
        }
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
