package ru.octoshell.bot.service.statemachine.listeners;

import com.google.common.collect.ImmutableMap;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.service.BotLinkService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.remote.RemoteCommandsService;
import ru.octoshell.bot.service.statemachine.UserState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class MainMenuStateListener implements StateListener {

    private static final String MESSAGE = "Нажмите на кнопку в меню";

    private final BotLinkService botLinkService;
    private final RemoteCommandsService remoteCommandsService;

    public MainMenuStateListener(BotLinkService botLinkService,
                                 RemoteCommandsService remoteCommandsService) {
        this.botLinkService = botLinkService;
        this.remoteCommandsService = remoteCommandsService;
    }

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
                    case SHOW_USER_PROJECTS:
                        showUserProjects(bot, update);
                        break;
                    default:
                        break;
                }
            }
        }

        return userState;
    }

    private void showUserProjects(OctoshellTelegramBot bot, Update update) {
        Integer userId = update.getMessage().getFrom().getId();
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
                sendMessage.setText("Установите корректные данные для аутентификации");
            } else {
                StringBuilder sb = new StringBuilder();
                JSONArray projArray = jsonObject.getJSONArray("projects");
                sb.append(":books: Проектов пользователя: ").append(projArray.length()).append("\n");

                for (int i = 0; i < projArray.length(); i++) {
                    JSONObject proj = projArray.getJSONObject(i);
                    sb.append("\n");
                    sb.append(":open_book: Проект #").append(i + 1).append("\n");
                    sb.append("Логин пользователя \"").append(proj.getString("login")).append("\"\n");
                    sb.append("Название \"").append(proj.getString("title")).append("\"\n");
                    if (proj.getBoolean("owner")) {
                        sb.append("Является владельцем\n");
                    } else {
                        sb.append("Не является владельцем\n");
                    }
                }
                sendMessage.setText(EmojiParser.parseToUnicode(sb.toString()));
            }
        } catch (Exception e) {
            log.error("Something wrong with showUserProjects()");
            log.error(e.toString());
            sendMessage.setText("Сервис Octoshell временно недоступен");
        }

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

        keyboard.add(buildRow(Button.TO_AUTH_SETTINGS));
        keyboard.add(buildRow(Button.SHOW_USER_PROJECTS));

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
        TO_AUTH_SETTINGS("\uD83D\uDD10 Настройки аутентификации"),
        SHOW_USER_PROJECTS(":books: Показать проекты пользователя");

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
