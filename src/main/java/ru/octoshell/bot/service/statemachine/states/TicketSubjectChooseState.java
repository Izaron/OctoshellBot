package ru.octoshell.bot.service.statemachine.states;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.handler.extra.ExtraDataService;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;

import java.util.Objects;

@Slf4j
@Service
public class TicketSubjectChooseState implements State {

    private final LocaleService localeService;
    private final UserStateService userStateService;
    private final ExtraDataService extraDataService;

    public TicketSubjectChooseState(LocaleService localeService,
                                    UserStateService userStateService,
                                    ExtraDataService extraDataService) {
        this.localeService = localeService;
        this.userStateService = userStateService;
        this.extraDataService = extraDataService;
    }

    @Override
    public Pair<UserState, Reaction> transition(Update update) {
        String text = update.getText();
        if (!Objects.isNull(text)) {
            Integer userId = update.getUserId();
            extraDataService.put(userId, "subject", text);

            return Pair.of(UserState.TICKET_MESSAGE_CHOOSE, null);
        }

        return null;
    }

    @Override
    public Reaction explain(Update update) {
        Integer userId = update.getUserId();
        String locale = userStateService.getUserLocale(userId);

        Reaction reaction = new Reaction();
        reaction.setForceReply(true);
        reaction.setText(localeService.get(locale, "main.tickets.subject.message"));
        return reaction;
    }
}
