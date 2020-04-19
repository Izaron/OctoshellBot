package ru.octoshell.bot.service.statemachine.states;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.remote.core.RemoteCommandsService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;

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
    public Pair<UserState, Reaction> transition(Update update) {
        String text = update.getText();
        if (Objects.isNull(text)) {
            return null;
        }

        String locale = userStateService.getUserLocale(update.getUserId());
        Button button = Button.findByText(localeService, locale, text);

        if (Objects.nonNull(button)) {
            switch (button) {
                case TO_AUTH_SETTINGS:
                    return Pair.of(UserState.AUTH_SETTINGS, null);
                case SHOW_USER_PROJECTS:
                    return Pair.of(null, showUserProjects(update));
                case SHOW_TICKETS:
                    Pair.of(null, showUserTickets(update));
                case CREATE_TICKETS:
                    return Pair.of(UserState.TICKET_PROJECT_CHOOSE, null);
                case TO_LOCALE_SETTINGS:
                    return Pair.of(UserState.LOCALE_SETTINGS, null);
                case SHOW_INFORMATION:
                    Pair.of(null, showInformation(update));
                default:
                    break;
            }
        }

        return null;
    }

    private Reaction showInformation(Update update) {
        Integer userId = update.getUserId();
        String locale = userStateService.getUserLocale(userId);

        Reaction reaction = new Reaction();
        reaction.setText(localeService.get(locale, "main.information"));
        return reaction;
    }

    private Reaction showUserProjects(Update update) {
        Integer userId = update.getUserId();
        String locale = userStateService.getUserLocale(userId);

        Reaction reaction = new Reaction();

        Map<String, String> map = ImmutableMap.of(
                "method", "user_projects"
        );

        try {
            JSONObject jsonObject = new JSONObject(remoteCommandsService.sendWithAuth(userId, map));
            String status = jsonObject.getString("status");
            if (StringUtils.equals(status, "fail")) {
                reaction.setText(localeService.get(locale, "main.fail-auth"));
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
                reaction.setText(sb.toString());
            }
        } catch (Exception e) {
            log.error("Something wrong with showUserProjects()");
            log.error(e.toString());
            reaction.setText(localeService.get(locale, "unavailable"));
        }

        return reaction;
    }

    private Reaction showUserTickets(Update update) {
        Integer userId = update.getUserId();
        String locale = userStateService.getUserLocale(userId);

        Reaction reaction = new Reaction();

        Map<String, String> map = ImmutableMap.of(
                "method", "user_tickets"
        );

        try {
            JSONObject jsonObject = new JSONObject(remoteCommandsService.sendWithAuth(userId, map));
            String status = jsonObject.getString("status");
            if (StringUtils.equals(status, "fail")) {
                reaction.setText(localeService.get(locale, "main.fail-auth"));
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
                reaction.setText(sb.toString());
            }
        } catch (Exception e) {
            log.error("Something wrong with showUserTickets()");
            log.error(e.toString());
            reaction.setText(localeService.get(locale, "unavailable"));
        }

        return reaction;
    }

    private List<String> buildRow(String locale, Button... buttons) {
        List<String> list = new ArrayList<>();
        for (Button button : buttons) {
            String desc = localeService.get(locale, button.getDesc());
            list.add(desc);
        }
        return list;
    }

    @Override
    public Reaction explain(Update update) {
        Integer userId = update.getUserId();
        String locale = userStateService.getUserLocale(userId);

        List<List<String>> keyboard = new ArrayList<>();
        keyboard.add(buildRow(locale, Button.SHOW_USER_PROJECTS));
        keyboard.add(buildRow(locale, Button.SHOW_TICKETS, Button.CREATE_TICKETS));
        keyboard.add(buildRow(locale, Button.TO_AUTH_SETTINGS));
        keyboard.add(buildRow(locale, Button.TO_LOCALE_SETTINGS));
        keyboard.add(buildRow(locale, Button.SHOW_INFORMATION));

        Reaction reaction = new Reaction();
        reaction.setText(localeService.get(locale, "main.message"));
        reaction.setKeyboard(keyboard);
        return reaction;
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
