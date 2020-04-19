package ru.octoshell.bot.service.statemachine.states;


import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.handler.extra.ExtraDataService;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.remote.core.RemoteCommandsService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class TicketMessageChooseState implements State {

    private final LocaleService localeService;
    private final UserStateService userStateService;
    private final ExtraDataService extraDataService;
    private final RemoteCommandsService remoteCommandsService;

    public TicketMessageChooseState(LocaleService localeService,
                                    UserStateService userStateService,
                                    ExtraDataService extraDataService,
                                    RemoteCommandsService remoteCommandsService) {
        this.localeService = localeService;
        this.userStateService = userStateService;
        this.extraDataService = extraDataService;
        this.remoteCommandsService = remoteCommandsService;
    }

    @Override
    public Pair<UserState, Reaction> transition(Update update) {
        String text = update.getText();
        if (!Objects.isNull(text)) {
            Integer userId = update.getUserId();
            extraDataService.put(userId, "message", text);

            Map<String, String> data = new HashMap<>();
            for (String key : ImmutableList.of("project", "topic", "cluster", "subject", "message")) {
                data.put(key, extraDataService.get(userId, key));
            }
            data.put("method", "create_ticket");

            remoteCommandsService.sendWithAuth(userId, data);

            return Pair.of(UserState.MAIN_MENU, null);
        }

        return null;
    }

    @Override
    public Reaction explain(Update update) {
        Integer userId = update.getUserId();
        String locale = userStateService.getUserLocale(userId);

        Reaction reaction = new Reaction();
        reaction.setForceReply(true);
        reaction.setText(localeService.get(locale, "main.tickets.message.message"));
        return reaction;
    }
}
