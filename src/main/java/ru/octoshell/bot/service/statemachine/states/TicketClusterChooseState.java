package ru.octoshell.bot.service.statemachine.states;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.handler.extra.ExtraDataService;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.remote.core.RemoteCommandsService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class TicketClusterChooseState implements State {

    private final LocaleService localeService;
    private final UserStateService userStateService;
    private final RemoteCommandsService remoteCommandsService;
    private final ExtraDataService extraDataService;

    public TicketClusterChooseState(LocaleService localeService,
                                    UserStateService userStateService,
                                    RemoteCommandsService remoteCommandsService,
                                    ExtraDataService extraDataService) {
        this.localeService = localeService;
        this.userStateService = userStateService;
        this.remoteCommandsService = remoteCommandsService;
        this.extraDataService = extraDataService;
    }

    @Override
    public Pair<UserState, Reaction> transition(Update update) {
        String text = update.getText();
        if (!Objects.isNull(text)) {
            Integer userId = update.getUserId();
            String locale = userStateService.getUserLocale(userId);

            if (StringUtils.equals(text, localeService.get(locale, "main.tickets.button.back"))) {
                return Pair.of(UserState.MAIN_MENU, null);
            }

            extraDataService.put(userId, "cluster", text);
            return Pair.of(UserState.TICKET_SUBJECT_CHOOSE, null);
        }

        return null;
    }

    @Override
    public Reaction explain(Update update) {
        Integer userId = update.getUserId();
        String locale = userStateService.getUserLocale(userId);

        Reaction reaction = new Reaction();
        List<List<String>> keyboard = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(remoteCommandsService.sendWithAuth(userId, ImmutableMap.of("method", "clusters")));
            String status = jsonObject.getString("status");
            if (StringUtils.equals(status, "fail")) {
                reaction.setText(localeService.get(locale, "main.fail-auth"));
            } else {
                reaction.setText(localeService.get(locale, "main.tickets.clusters.message"));

                JSONArray array = jsonObject.getJSONArray("clusters");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    keyboard.add(Arrays.asList(obj.getString("name_" + locale)));
                }
            }
        } catch (Exception e) {
            log.error("Something wrong with TicketClusterChooseStateListener::drawState()");
            log.error(e.toString());
            reaction.setText(localeService.get(locale, "unavailable"));
        }
        keyboard.add(Arrays.asList(localeService.get(locale, "main.tickets.button.back")));

        reaction.setKeyboard(keyboard);
        return reaction;
    }
}
