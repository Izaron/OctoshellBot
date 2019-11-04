package ru.octoshell.bot.service.statemachine.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.service.BotLinkService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.statemachine.UserState;

@Slf4j
@Service
public class AuthNewTokenStateListener implements StateListener {

    private static final String MESSAGE = "Введите новый токен";

    private final BotLinkService botLinkService;

    public AuthNewTokenStateListener(BotLinkService botLinkService) {
        this.botLinkService = botLinkService;
    }

    @Override
    public UserState processUpdate(UserState userState, OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            String text = message.getText();
            Integer userId = message.getFrom().getId();
            botLinkService.updateToken(userId, text);

            return UserState.AUTH_SETTINGS;
        }

        return userState;
    }

    @Override
    public void drawState(UserState userState, OctoshellTelegramBot bot, Message latestMessage) {
        SendMessage sendMessage = new SendMessage();

        ForceReplyKeyboard keyboard = new ForceReplyKeyboard();
        keyboard.setSelective(false);
        sendMessage.setReplyMarkup(keyboard);

        sendMessage.setChatId(latestMessage.getChatId().toString());
        sendMessage.setText(MESSAGE);

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }
}
