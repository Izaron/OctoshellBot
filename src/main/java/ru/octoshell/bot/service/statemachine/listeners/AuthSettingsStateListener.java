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
import ru.octoshell.bot.service.BotLinkService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.remote.AuthentificationService;
import ru.octoshell.bot.service.statemachine.UserState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AuthSettingsStateListener implements StateListener {

    private static final String MESSAGE = "Нажмите на кнопку в меню";

    private final BotLinkService botLinkService;
    private final AuthentificationService authentificationService;

    public AuthSettingsStateListener(BotLinkService botLinkService,
                                     AuthentificationService authentificationService) {
        this.botLinkService = botLinkService;
        this.authentificationService = authentificationService;
    }

    @Override
    public UserState processUpdate(UserState userState, OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();

        if (message.hasText()) {
            String text = message.getText();
            Button button = Button.findByDesc(text);

            if (Objects.nonNull(button)) {
                switch (button) {
                    case BACK:
                        return UserState.MAIN_MENU;
                    case CHANGE_EMAIL:
                        return UserState.AUTH_NEW_EMAIL;
                    case CHANGE_TOKEN:
                        return UserState.AUTH_NEW_TOKEN;
                    case SHOW_SETTINGS:
                        showSettings(bot, update);
                        break;
                    case CHECK_CONNECTION:
                        checkConnection(bot, update);
                        break;
                    default:
                        break;
                }
            }
        }

        return userState;
    }

    private void showSettings(OctoshellTelegramBot bot, Update update) {
        Integer userId = update.getMessage().getFrom().getId();
        String email = StringUtils.defaultIfEmpty(botLinkService.getEmail(userId), "<Пустой e-mail>");
        String token = StringUtils.defaultIfEmpty(botLinkService.getToken(userId), "<Пустой токен>");

        StringBuilder sb = new StringBuilder();
        sb.append(":gear: Настройки аутентификации\n");
        sb.append("E-mail: ").append(email).append("\n");
        sb.append("Токен: ").append(token).append("\n");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId.toString());
        sendMessage.setText(EmojiParser.parseToUnicode(sb.toString()));

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }

    private void checkConnection(OctoshellTelegramBot bot, Update update) {
        Integer userId = update.getMessage().getFrom().getId();
        String email = StringUtils.defaultString(botLinkService.getEmail(userId));
        String token = StringUtils.defaultString(botLinkService.getToken(userId));

        AuthentificationService.AuthStatus authStatus =
                authentificationService.authentificate(email, token);

        StringBuilder sb = new StringBuilder();
        sb.append(":crystal_ball: Проверка подключения\n");
        sb.append(authStatus.getDescription());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId.toString());
        sendMessage.setText(EmojiParser.parseToUnicode(sb.toString()));

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
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

        keyboard.add(buildRow(Button.CHANGE_EMAIL, Button.CHANGE_TOKEN));
        keyboard.add(buildRow(Button.SHOW_SETTINGS));
        keyboard.add(buildRow(Button.CHECK_CONNECTION));
        keyboard.add(buildRow(Button.BACK));

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
        CHANGE_EMAIL(":e-mail: Поменять e-mail"),
        CHANGE_TOKEN(":key: Поменять токен"),
        SHOW_SETTINGS(":eyes: Вывести текущие настройки авторизации"),
        CHECK_CONNECTION(":electric_plug: Проверка подключения к Octoshell"),
        BACK(":door: Назад");

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
