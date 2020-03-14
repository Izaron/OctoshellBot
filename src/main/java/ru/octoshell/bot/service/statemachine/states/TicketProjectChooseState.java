package ru.octoshell.bot.service.statemachine.states;

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
import ru.octoshell.bot.service.handler.extra.ExtraDataService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.remote.core.RemoteCommandsService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.handler.userstate.UserStateService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TicketProjectChooseState implements State {

    private final LocaleService localeService;
    private final UserStateService userStateService;
    private final RemoteCommandsService remoteCommandsService;
    private final ExtraDataService extraDataService;

    public TicketProjectChooseState(LocaleService localeService,
                                    UserStateService userStateService,
                                    RemoteCommandsService remoteCommandsService,
                                    ExtraDataService extraDataService) {
        this.localeService = localeService;
        this.userStateService = userStateService;
        this.extraDataService = extraDataService;
        this.remoteCommandsService = remoteCommandsService;
    }

    private KeyboardRow buildRow(String... buttons) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.addAll(Arrays.asList(buttons));
        return keyboardRow;
    }

    @Override
    public UserState transition(OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();

        if (message.hasText()) {
            String text = message.getText();
            User user = message.getFrom();
            Integer userId = user.getId();
            String locale = userStateService.getUserLocale(user.getId());

            if (StringUtils.equals(text, localeService.get(locale, "main.tickets.button.back"))) {
                return UserState.MAIN_MENU;
            }

            extraDataService.put(userId, "project", text);
            return UserState.TICKET_TOPIC_CHOOSE;
        }

        return null;
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

        try {
            JSONObject jsonObject = new JSONObject(remoteCommandsService.sendWithAuth(userId, ImmutableMap.of("method", "user_projects")));
            String status = jsonObject.getString("status");
            if (StringUtils.equals(status, "fail")) {
                sendMessage.setText(localeService.get(locale, "main.fail-auth"));
            } else {
                sendMessage.setText(localeService.get(locale, "main.tickets.projects.message"));

                JSONArray projArray = jsonObject.getJSONArray("projects");
                for (int i = 0; i < projArray.length(); i++) {
                    JSONObject proj = projArray.getJSONObject(i);
                    keyboard.add(buildRow(proj.getString("title")));
                }
            }
        } catch (Exception e) {
            log.error("Something wrong with TicketProjectChooseStateListener::drawState()");
            log.error(e.toString());
            sendMessage.setText(localeService.get(locale, "unavailable"));
        }
        keyboard.add(buildRow(localeService.get(locale, "main.tickets.button.back")));

        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(message.getChatId().toString());

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }
}
