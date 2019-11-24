package ru.octoshell.bot.service.statemachine.listeners;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
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
import ru.octoshell.bot.service.remote.RemoteCommandsService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.UserStateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class MainMenuStateListener implements StateListener {

    private final BotLinkService botLinkService;
    private final RemoteCommandsService remoteCommandsService;
    private final UserStateService userStateService;
    private final LocaleService localeService;

    public MainMenuStateListener(BotLinkService botLinkService,
                                 RemoteCommandsService remoteCommandsService,
                                 UserStateService userStateService,
                                 LocaleService localeService) {
        this.botLinkService = botLinkService;
        this.remoteCommandsService = remoteCommandsService;
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
                    case TO_AUTH_SETTINGS:
                        return UserState.AUTH_SETTINGS;
                    case SHOW_USER_PROJECTS:
                        showUserProjects(bot, update);
                        break;
                    case TO_LOCALE_SETTINGS:
                        return UserState.LOCALE_SETTINGS;
                    default:
                        break;
                }
            }
        }

        return userState;
    }

    private void showUserProjects(OctoshellTelegramBot bot, Update update) {
        Integer userId = update.getMessage().getFrom().getId();
        String locale = userStateService.getUserLocale(userId);
        String email = StringUtils.defaultString(botLinkService.getEmail(userId));
        String token = StringUtils.defaultString(botLinkService.getToken(userId));

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId.toString());

        Map<String, String> map = ImmutableMap.of(
                "method", "user_projects",
                "email", email,
                "token", token
        );

        try {
            JSONObject jsonObject = remoteCommandsService.sendCommandJson(map);
            String status = jsonObject.getString("status");
            if (StringUtils.equals(status, "fail")) {
                sendMessage.setText(localeService.getProperty(locale, "main.fail-auth"));
            } else {
                StringBuilder sb = new StringBuilder();
                JSONArray projArray = jsonObject.getJSONArray("projects");
                sb.append(localeService.getProperty(locale, "main.projects.header"))
                        .append(" ")
                        .append(projArray.length())
                        .append("\n");

                for (int i = 0; i < projArray.length(); i++) {
                    JSONObject proj = projArray.getJSONObject(i);
                    sb.append("\n");
                    sb.append(localeService.getProperty(locale, "main.projects.number"))
                            .append(i + 1).append("\n");
                    sb.append(localeService.getProperty(locale, "main.projects.login"))
                            .append(" \"").append(proj.getString("login")).append("\"\n");
                    sb.append(localeService.getProperty(locale, "main.projects.title"))
                            .append(" \"").append(proj.getString("title")).append("\"\n");
                    if (proj.getBoolean("owner")) {
                        sb.append(localeService.getProperty(locale, "main.projects.is-owner"))
                                .append("\n");
                    } else {
                        sb.append(localeService.getProperty(locale, "main.projects.is-not-owner"))
                                .append("\n");
                    }
                }
                sendMessage.setText(sb.toString());
            }
        } catch (Exception e) {
            log.error("Something wrong with showUserProjects()");
            log.error(e.toString());
            sendMessage.setText(localeService.getProperty(locale, "unavailable"));
        }

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

        keyboard.add(buildRow(locale, Button.SHOW_USER_PROJECTS));
        keyboard.add(buildRow(locale, Button.TO_AUTH_SETTINGS));
        keyboard.add(buildRow(locale, Button.TO_LOCALE_SETTINGS));

        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(latestMessage.getChatId().toString());
        sendMessage.setText(localeService.getProperty(locale, "main.message"));

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }

    private enum Button {
        SHOW_USER_PROJECTS("main.button.show-user-projects"),
        TO_AUTH_SETTINGS("main.button.to-auth-settings"),
        TO_LOCALE_SETTINGS("main.button.to-locale-settings");

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
