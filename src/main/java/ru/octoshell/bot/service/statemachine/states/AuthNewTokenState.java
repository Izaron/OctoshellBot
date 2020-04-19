package ru.octoshell.bot.service.statemachine.states;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.handler.botlink.BotLinkService;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;

import java.util.Objects;

@Slf4j
@Service
public class AuthNewTokenState implements State {

    private final BotLinkService botLinkService;
    private final LocaleService localeService;
    private final UserStateService userStateService;

    public AuthNewTokenState(BotLinkService botLinkService,
                             LocaleService localeService,
                             UserStateService userStateService) {
        this.botLinkService = botLinkService;
        this.localeService = localeService;
        this.userStateService = userStateService;
    }

    @Override
    public Pair<UserState, Reaction> transition(Update update) {
        String text = update.getText();
        if (!Objects.isNull(text)) {
            Integer userId = update.getUserId();
            botLinkService.updateToken(userId, text);

            return Pair.of(UserState.AUTH_SETTINGS, null);
        }

        return null;
    }

    @Override
    public Reaction explain(Update update) {
        Integer userId = update.getUserId();
        String locale = userStateService.getUserLocale(userId);

        Reaction reaction = new Reaction();
        reaction.setText(localeService.get(locale, "auth.token.message"));
        reaction.setForceReply(true);
        return reaction;
    }
}
