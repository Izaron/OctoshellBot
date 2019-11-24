package ru.octoshell.bot.service.statemachine.listeners;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.service.BotLinkService;
import ru.octoshell.bot.service.LocaleService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.remote.AuthentificationService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.UserStateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AuthSettingsStateListener implements StateListener {

    private final BotLinkService botLinkService;
    private final AuthentificationService authentificationService;
    private final UserStateService userStateService;
    private final LocaleService localeService;

    public AuthSettingsStateListener(BotLinkService botLinkService,
                                     AuthentificationService authentificationService,
                                     UserStateService userStateService,
                                     LocaleService localeService) {
        this.botLinkService = botLinkService;
        this.authentificationService = authentificationService;
        this.userStateService = userStateService;
        this.localeService = localeService;
    }

    @Override
    public UserState processUpdate(UserState userState, OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();

        if (message.hasText()) {
            String text = message.getText();
            User user = message.getFrom();
            String locale = userStateService.getUserLocale(user.getId());
            Button button = Button.findByText(localeService, locale, text);

            if (Objects.nonNull(button)) {
                switch (button) {
                    case CHANGE_EMAIL:
                        return UserState.AUTH_NEW_EMAIL;
                    case CHANGE_TOKEN:
                        return UserState.AUTH_NEW_TOKEN;
                    case SHOW_SETTINGS:
                        showSettings(locale, bot, update);
                        break;
                    case CHECK_CONNECTION:
                        checkConnection(locale, bot, update);
                        break;
                    default:
                        return UserState.MAIN_MENU;
                }
            }
        }

        return userState;
    }

    private void showSettings(String locale, OctoshellTelegramBot bot, Update update) {
        Integer userId = update.getMessage().getFrom().getId();
        String email = StringUtils.defaultIfEmpty(botLinkService.getEmail(userId),
                localeService.getProperty(locale, "auth.blank-email"));
        String token = StringUtils.defaultIfEmpty(botLinkService.getToken(userId),
                localeService.getProperty(locale, "auth.blank-token"));

        StringBuilder sb = new StringBuilder();
        sb.append(localeService.getProperty(locale, "auth.settings.header"))
                .append("\n");
        sb.append(localeService.getProperty(locale, "auth.settings.email"))
                .append(": ").append(email).append("\n");
        sb.append(localeService.getProperty(locale, "auth.settings.token"))
                .append(": ").append(token).append("\n");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId.toString());
        sendMessage.setText(sb.toString());

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }

    private void checkConnection(String locale, OctoshellTelegramBot bot, Update update) {
        Integer userId = update.getMessage().getFrom().getId();
        String email = StringUtils.defaultString(botLinkService.getEmail(userId));
        String token = StringUtils.defaultString(botLinkService.getToken(userId));

        AuthentificationService.AuthStatus authStatus =
                authentificationService.authentificate(email, token);

        StringBuilder sb = new StringBuilder();
        sb.append(localeService.getProperty(locale, "auth.check.header")).append("\n");
        sb.append(localeService.getProperty(locale, authStatus.getDescription()));

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId.toString());
        sendMessage.setText(sb.toString());

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }

    private KeyboardRow buildRow(String locale, Button... buttons) {
        KeyboardRow keyboardRow = new KeyboardRow();
        for (Button button : buttons) {
            String desc = localeService.getProperty(locale, button.getDesc());
            keyboardRow.add(desc);
        }
        return keyboardRow;
    }

    @Override
    public void drawState(UserState userState, OctoshellTelegramBot bot, Message latestMessage) {
        Integer userId = latestMessage.getFrom().getId();
        String locale = userStateService.getUserLocale(userId);

        SendMessage sendMessage = new SendMessage();

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        keyboard.add(buildRow(locale, Button.CHANGE_EMAIL, Button.CHANGE_TOKEN));
        keyboard.add(buildRow(locale, Button.SHOW_SETTINGS));
        keyboard.add(buildRow(locale, Button.CHECK_CONNECTION));
        keyboard.add(buildRow(locale, Button.BACK));

        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(latestMessage.getChatId().toString());
        sendMessage.setText(localeService.getProperty(locale, "auth.message"));

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }

    private enum Button {
        CHANGE_EMAIL("auth.button.change-email"),
        CHANGE_TOKEN("auth.button.change-token"),
        SHOW_SETTINGS("auth.button.show-settings"),
        CHECK_CONNECTION("auth.button.check-connection"),
        BACK("auth.button.back");

        private final String desc;

        Button(String desc) {
            this.desc = desc;
        }

        public static Button findByText(LocaleService localeService, String locale, String text) {
            for (Button button : values()) {
                if (StringUtils.equals(localeService.getProperty(locale, button.getDesc()), text)) {
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
