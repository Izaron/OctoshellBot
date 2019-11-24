package ru.octoshell.bot.service.statemachine.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.statemachine.UserState;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class StateListenersService implements StateListener {

    private final MainMenuStateListener mainMenuStateListener;
    private final AuthSettingsStateListener authSettingsStateListener;
    private final AuthNewEmailStateListener authNewEmailStateListener;
    private final AuthNewTokenStateListener authNewTokenStateListener;
    private final LocaleSettingsStateListener localeSettingsStateListener;

    private List<StateListener> listenerList;

    public StateListenersService(MainMenuStateListener mainMenuStateListener,
                                 AuthSettingsStateListener authSettingsStateListener,
                                 AuthNewEmailStateListener authNewEmailStateListener,
                                 AuthNewTokenStateListener authNewTokenStateListener,
                                 LocaleSettingsStateListener localeSettingsStateListener) {
        this.mainMenuStateListener = mainMenuStateListener;
        this.authSettingsStateListener = authSettingsStateListener;
        this.authNewEmailStateListener = authNewEmailStateListener;
        this.authNewTokenStateListener = authNewTokenStateListener;
        this.localeSettingsStateListener = localeSettingsStateListener;

        listenerList = Arrays.asList(
                mainMenuStateListener,
                authSettingsStateListener,
                authNewEmailStateListener,
                authNewTokenStateListener,
                localeSettingsStateListener
        );
    }

    @Override
    public UserState processUpdate(UserState userState, OctoshellTelegramBot bot, Update update) {
        StateListener listener = findListenerByUserState(userState);
        if (Objects.nonNull(listener)) {
            return listener.processUpdate(userState, bot, update);
        } else {
            log.error("Unknown user state '{}'!", userState);
            return UserState.getDefaultState();
        }
    }

    @Override
    public void drawState(UserState userState, OctoshellTelegramBot bot, Message latestMessage) {
        StateListener listener = findListenerByUserState(userState);
        if (Objects.nonNull(listener)) {
            listener.drawState(userState, bot, latestMessage);
        } else {
            log.error("Unknown user state '{}'!", userState);
        }
    }

    private StateListener findListenerByUserState(UserState userState) {
        Class targetClass = userState.getListenerClass();
        for (StateListener stateListener : listenerList) {
            if (stateListener.getClass() == targetClass) {
                return stateListener;
            }
        }
        return null;
    }
}
