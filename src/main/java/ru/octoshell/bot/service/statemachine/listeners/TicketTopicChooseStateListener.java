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
import ru.octoshell.bot.service.ExtraDataService;
import ru.octoshell.bot.service.LocaleService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.remote.RemoteCommandsService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.UserStateService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TicketTopicChooseStateListener implements StateListener {

    private final LocaleService localeService;
    private final UserStateService userStateService;
    private final RemoteCommandsService remoteCommandsService;
    private final ExtraDataService extraDataService;

    public TicketTopicChooseStateListener(LocaleService localeService,
                                          UserStateService userStateService,
                                          RemoteCommandsService remoteCommandsService,
                                          ExtraDataService extraDataService) {
        this.localeService = localeService;
        this.userStateService = userStateService;
        this.remoteCommandsService = remoteCommandsService;
        this.extraDataService = extraDataService;
    }

    private KeyboardRow buildRow(String... buttons) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.addAll(Arrays.asList(buttons));
        return keyboardRow;
    }

    @Override
    public UserState processUpdate(UserState userState, OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();

        if (message.hasText()) {
            String text = message.getText();
            User user = message.getFrom();
            Integer userId = user.getId();
            String locale = userStateService.getUserLocale(user.getId());

            if (StringUtils.equals(text, localeService.getProperty(locale, "main.tickets.button.back"))) {
                return UserState.MAIN_MENU;
            }

            extraDataService.put(userId, "topic", text);
            return UserState.TICKET_CLUSTER_CHOOSE;
        }

        return userState;
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

        try {
            JSONObject jsonObject = remoteCommandsService.sendCommandWithAuth(userId, ImmutableMap.of("method", "topics"));
            String status = jsonObject.getString("status");
            if (StringUtils.equals(status, "fail")) {
                sendMessage.setText(localeService.getProperty(locale, "main.fail-auth"));
            } else {
                sendMessage.setText(localeService.getProperty(locale, "main.tickets.topics.message"));

                JSONArray topicArray = jsonObject.getJSONArray("topics");
                for (int i = 0; i < topicArray.length(); i++) {
                    JSONObject topic = topicArray.getJSONObject(i);
                    keyboard.add(buildRow(topic.getString("name_" + locale)));
                }
            }
        } catch (Exception e) {
            log.error("Something wrong with TicketTopicChooseStateListener::drawState()");
            log.error(e.toString());
            sendMessage.setText(localeService.getProperty(locale, "unavailable"));
        }
        keyboard.add(buildRow(localeService.getProperty(locale, "main.tickets.button.back")));

        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(latestMessage.getChatId().toString());

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }
}
