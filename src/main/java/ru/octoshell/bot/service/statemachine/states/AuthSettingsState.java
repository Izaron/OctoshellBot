package ru.octoshell.bot.service.statemachine.states;

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
import ru.octoshell.bot.service.handler.botlink.BotLinkService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.remote.wrappers.auth.AuthStatus;
import ru.octoshell.bot.service.remote.wrappers.auth.AuthenticationService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.handler.userstate.UserStateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AuthSettingsState implements State {

    private final BotLinkService botLinkService;
    private final AuthenticationService authenticationService;
    private final UserStateService userStateService;
    private final LocaleService localeService;

    public AuthSettingsState(BotLinkService botLinkService,
                             AuthenticationService authenticationService,
                             UserStateService userStateService,
                             LocaleService localeService) {
        this.botLinkService = botLinkService;
        this.authenticationService = authenticationService;
        this.userStateService = userStateService;
        this.localeService = localeService;
    }

    @Override
    public UserState transition(OctoshellTelegramBot bot, Update update) {
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

        return null;
    }

    private void showSettings(String locale, OctoshellTelegramBot bot, Update update) {
        Integer userId = update.getMessage().getFrom().getId();
        String email = StringUtils.defaultIfEmpty(botLinkService.getEmail(userId),
                localeService.get(locale, "auth.blank-email"));
        String token = StringUtils.defaultIfEmpty(botLinkService.getToken(userId),
                localeService.get(locale, "auth.blank-token"));

        StringBuilder sb = new StringBuilder();
        sb.append(localeService.get(locale, "auth.settings.header"))
                .append("\n");
        sb.append(localeService.get(locale, "auth.settings.email"))
                .append(": ").append(email).append("\n");
        sb.append(localeService.get(locale, "auth.settings.token"))
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

        AuthStatus authStatus = authenticationService.authenticate(userId);

        StringBuilder sb = new StringBuilder();
        sb.append(localeService.get(locale, "auth.check.header")).append("\n");
        sb.append(localeService.get(locale, authStatus.getDescription()));

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
            String desc = localeService.get(locale, button.getDesc());
            keyboardRow.add(desc);
        }
        return keyboardRow;
    }

    @Override
    public void explain(UserState userState, OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();
        Integer userId = message.getFrom().getId();
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
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(localeService.get(locale, "auth.message"));

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
                if (StringUtils.equals(localeService.get(locale, button.getDesc()), text)) {
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
