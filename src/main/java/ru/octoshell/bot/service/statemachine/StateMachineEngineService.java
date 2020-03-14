package ru.octoshell.bot.service.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.statemachine.states.transistor.StateTransistorService;

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

    public void processUpdate(OctoshellTelegramBot bot, Update update) {
        // Filter wrong messages
        Message message = update.getMessage();
        if (!message.isUserMessage()) {
            log.info("Is not user message!");
            return;
        }

        User user = message.getFrom();
        if (Objects.isNull(user)) {
            log.info("Have no 'from' field in the message!");
            return;
        }

        // Change state and draw it
        Integer userId = user.getId();
        UserState userState = userStateService.getUserState(userId);

        UserState newUserState = stateTransistorService.transition(userState, bot, update);
        stateTransistorService.explain(newUserState, bot, update);

        userStateService.setUserState(userId, newUserState);
    }
}
