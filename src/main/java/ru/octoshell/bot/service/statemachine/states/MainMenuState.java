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
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.remote.core.RemoteCommandsService;
import ru.octoshell.bot.service.statemachine.UserState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class MainMenuState implements State {

    private final RemoteCommandsService remoteCommandsService;
    private final UserStateService userStateService;
    private final LocaleService localeService;

    public MainMenuState(RemoteCommandsService remoteCommandsService,
                         UserStateService userStateService,
                         LocaleService localeService) {
        this.remoteCommandsService = remoteCommandsService;
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
                    case TO_AUTH_SETTINGS:
                        return UserState.AUTH_SETTINGS;
                    case SHOW_USER_PROJECTS:
                        showUserProjects(bot, update);
                        break;
                    case SHOW_TICKETS:
                        showUserTickets(bot, update);
                        break;
                    case CREATE_TICKETS:
                        return UserState.TICKET_PROJECT_CHOOSE;
                    case TO_LOCALE_SETTINGS:
                        return UserState.LOCALE_SETTINGS;
                    case SHOW_INFORMATION:
                        showInformation(bot, update);
                        break;
                    default:
                        break;
                }
            }
        }

        return null;
    }

    private void showInformation(OctoshellTelegramBot bot, Update update) {
        Integer userId = update.getMessage().getFrom().getId();
        String locale = userStateService.getUserLocale(userId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId.toString());
        sendMessage.setText(localeService.get(locale, "main.information"));

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }

    private void showUserProjects(OctoshellTelegramBot bot, Update update) {
        Integer userId = update.getMessage().getFrom().getId();
        String locale = userStateService.getUserLocale(userId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId.toString());

        Map<String, String> map = ImmutableMap.of(
                "method", "user_projects"
        );

        try {
            JSONObject jsonObject = new JSONObject(remoteCommandsService.sendWithAuth(userId, map));
            String status = jsonObject.getString("status");
            if (StringUtils.equals(status, "fail")) {
                sendMessage.setText(localeService.get(locale, "main.fail-auth"));
            } else {
                StringBuilder sb = new StringBuilder();
                JSONArray projArray = jsonObject.getJSONArray("projects");
                sb.append(localeService.get(locale, "main.projects.header"))
                        .append(" ")
                        .append(projArray.length())
                        .append("\n");

                for (int i = 0; i < projArray.length(); i++) {
                    JSONObject proj = projArray.getJSONObject(i);
                    sb.append("\n");
                    sb.append(localeService.get(locale, "main.projects.number"))
                            .append(i + 1).append("\n");
                    sb.append(localeService.get(locale, "main.projects.login"))
                            .append(" \"").append(proj.getString("login")).append("\"\n");
                    sb.append(localeService.get(locale, "main.projects.title"))
                            .append(" \"").append(proj.getString("title")).append("\"\n");
                    if (proj.getBoolean("owner")) {
                        sb.append(localeService.get(locale, "main.projects.is-owner"))
                                .append("\n");
                    } else {
                        sb.append(localeService.get(locale, "main.projects.is-not-owner"))
                                .append("\n");
                    }
                }
                sendMessage.setText(sb.toString());
            }
        } catch (Exception e) {
            log.error("Something wrong with showUserProjects()");
            log.error(e.toString());
            sendMessage.setText(localeService.get(locale, "unavailable"));
        }

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }

    private void showUserTickets(OctoshellTelegramBot bot, Update update) {
        Integer userId = update.getMessage().getFrom().getId();
        String locale = userStateService.getUserLocale(userId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId.toString());

        Map<String, String> map = ImmutableMap.of(
                "method", "user_tickets"
        );

        try {
            JSONObject jsonObject = new JSONObject(remoteCommandsService.sendWithAuth(userId, map));
            String status = jsonObject.getString("status");
            if (StringUtils.equals(status, "fail")) {
                sendMessage.setText(localeService.get(locale, "main.fail-auth"));
            } else {
                StringBuilder sb = new StringBuilder();
                JSONArray ticketsArray = jsonObject.getJSONArray("tickets");
                sb.append(localeService.get(locale, "main.tickets.header"))
                        .append(" ")
                        .append(ticketsArray.length())
                        .append("\n");

                for (int i = 0; i < ticketsArray.length(); i++) {
                    JSONObject ticket = ticketsArray.getJSONObject(i);

                    String who = localeService.get(locale, "main.tickets.who-is-" +
                            ticket.getString("who"));
                    String topicName = ticket.getString("topic_name_" + locale);
                    String projectTitle = ticket.getString("project_title");
                    String clusterName = ticket.getString("cluster_name_" + locale);
                    String subject = ticket.getString("subject");
                    String message = ticket.getString("message");
                    String state = ticket.getString("state");

                    sb.append("\n");
                    sb.append(localeService.get(locale, "main.tickets.number"))
                            .append(i + 1).append("\n");
                    sb.append(localeService.get(locale, "main.tickets.who-status"))
                            .append(": ").append(who).append("\n");
                    sb.append(localeService.get(locale, "main.tickets.topic"))
                            .append(": ").append(topicName).append("\n");
                    sb.append(localeService.get(locale, "main.tickets.project"))
                            .append(": ").append(projectTitle).append("\n");
                    sb.append(localeService.get(locale, "main.tickets.cluster"))
                            .append(": ").append(clusterName).append("\n");
                    sb.append(localeService.get(locale, "main.tickets.subject"))
                            .append(": ").append(subject).append("\n");
                    sb.append(localeService.get(locale, "main.tickets.state"))
                            .append(": ")
                            .append(localeService.get(locale, "main.tickets.state." + state))
                            .append("\n");
                    sb.append(localeService.get(locale, "main.tickets.desc"))
                            .append(": ").append(message).append("\n");
                }
                sendMessage.setText(sb.toString());


            }
        } catch (Exception e) {
            log.error("Something wrong with showUserTickets()");
            log.error(e.toString());
            sendMessage.setText(localeService.get(locale, "unavailable"));
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

        keyboard.add(buildRow(locale, Button.SHOW_USER_PROJECTS));
        keyboard.add(buildRow(locale, Button.SHOW_TICKETS, Button.CREATE_TICKETS));
        keyboard.add(buildRow(locale, Button.TO_AUTH_SETTINGS));
        keyboard.add(buildRow(locale, Button.TO_LOCALE_SETTINGS));
        keyboard.add(buildRow(locale, Button.SHOW_INFORMATION));

        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(localeService.get(locale, "main.message"));

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }

    private enum Button {
        SHOW_USER_PROJECTS("main.button.show-user-projects"),
        TO_AUTH_SETTINGS("main.button.to-auth-settings"),
        TO_LOCALE_SETTINGS("main.button.to-locale-settings"),
        SHOW_TICKETS("main.button.show-tickets"),
        CREATE_TICKETS("main.button.create-tickets"),
        SHOW_INFORMATION("main.button.information");

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
