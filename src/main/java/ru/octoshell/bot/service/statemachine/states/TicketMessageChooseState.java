package ru.octoshell.bot.service.statemachine.states;


import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.service.handler.extra.ExtraDataService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.remote.core.RemoteCommandsService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.handler.userstate.UserStateService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TicketMessageChooseState implements State {

    private final LocaleService localeService;
    private final UserStateService userStateService;
    private final ExtraDataService extraDataService;
    private final RemoteCommandsService remoteCommandsService;

    public TicketMessageChooseState(LocaleService localeService,
                                    UserStateService userStateService,
                                    ExtraDataService extraDataService,
                                    RemoteCommandsService remoteCommandsService) {
        this.localeService = localeService;
        this.userStateService = userStateService;
        this.extraDataService = extraDataService;
        this.remoteCommandsService = remoteCommandsService;
    }

    @Override
    public UserState transition(OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            String text = message.getText();
            Integer userId = message.getFrom().getId();
            extraDataService.put(userId, "message", text);

            Map<String, String> data = new HashMap<>();
            for (String key : ImmutableList.of("project", "topic", "cluster", "subject", "message")) {
                data.put(key, extraDataService.get(userId, key));
            }
            data.put("method", "create_ticket");

            remoteCommandsService.sendWithAuth(userId, data);

            return UserState.MAIN_MENU;
        }

        return null;
    }

    @Override
    public void explain(UserState userState, OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();
        Integer userId = message.getFrom().getId();
        String locale = userStateService.getUserLocale(userId);

        SendMessage sendMessage = new SendMessage();

        ForceReplyKeyboard keyboard = new ForceReplyKeyboard();
        keyboard.setSelective(false);
        sendMessage.setReplyMarkup(keyboard);

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(localeService.get(locale, "main.tickets.message.message"));

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }
}
