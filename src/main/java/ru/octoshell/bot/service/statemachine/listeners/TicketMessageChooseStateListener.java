package ru.octoshell.bot.service.statemachine.listeners;


import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.service.ExtraDataService;
import ru.octoshell.bot.service.LocaleService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.remote.RemoteCommandsService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.UserStateService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TicketMessageChooseStateListener implements StateListener {

    private final LocaleService localeService;
    private final UserStateService userStateService;
    private final ExtraDataService extraDataService;
    private final RemoteCommandsService remoteCommandsService;

    public TicketMessageChooseStateListener(LocaleService localeService,
                                            UserStateService userStateService,
                                            ExtraDataService extraDataService,
                                            RemoteCommandsService remoteCommandsService) {
        this.localeService = localeService;
        this.userStateService = userStateService;
        this.extraDataService = extraDataService;
        this.remoteCommandsService = remoteCommandsService;
    }

    @Override
    public UserState processUpdate(UserState userState, OctoshellTelegramBot bot, Update update) {
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

            remoteCommandsService.sendCommandWithAuth(userId, data);

            return UserState.MAIN_MENU;
        }

        return userState;
    }

    @Override
    public void drawState(UserState userState, OctoshellTelegramBot bot, Message latestMessage) {
        Integer userId = latestMessage.getFrom().getId();
        String locale = userStateService.getUserLocale(userId);

        SendMessage sendMessage = new SendMessage();

        ForceReplyKeyboard keyboard = new ForceReplyKeyboard();
        keyboard.setSelective(false);
        sendMessage.setReplyMarkup(keyboard);

        sendMessage.setChatId(latestMessage.getChatId().toString());
        sendMessage.setText(localeService.getProperty(locale, "main.tickets.message.message"));

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }
}
