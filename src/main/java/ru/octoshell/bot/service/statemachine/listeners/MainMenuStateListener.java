package ru.octoshell.bot.service.statemachine.listeners;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.statemachine.UserState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class MainMenuStateListener implements StateListener {

    private static final String MESSAGE = "Нажмите на кнопку в меню";

    @Override
    public UserState processUpdate(UserState userState, OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();

        if (message.hasText()) {
            String text = message.getText();
            Button button = Button.findByDesc(text);

            if (Objects.nonNull(button)) {
                switch (button) {
                    case TO_AUTH_SETTINGS:
                        return UserState.AUTH_SETTINGS;
                    default:
                        break;
                }
            }
        }

        return userState;
    }

    private KeyboardRow buildRow(Button... buttons) {
        KeyboardRow keyboardRow = new KeyboardRow();
        for (Button button : buttons) {
            keyboardRow.add(button.getDesc());
        }
        return keyboardRow;
    }

    @Override
    public void drawState(UserState userState, OctoshellTelegramBot bot, Message latestMessage) {
        SendMessage sendMessage = new SendMessage();

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        keyboard.add(buildRow(Button.TO_AUTH_SETTINGS));

        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(latestMessage.getChatId().toString());
        sendMessage.setText(MESSAGE);

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }

    private enum Button {
        TO_AUTH_SETTINGS("\uD83D\uDD10 Настройки аутентификации");

        private final String desc;

        Button(String desc) {
            this.desc = EmojiParser.parseToUnicode(desc);
        }

        public static Button findByDesc(String desc) {
            for (Button button : values()) {
                if (StringUtils.equals(desc, button.getDesc())) {
                    return button;
                }
            }
            return null;
        }

        public String getDesc() {
            return desc;
        }
    }
}
