package ru.octoshell.bot.service.statemachine.states;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.handler.botlink.BotLinkService;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.remote.wrappers.auth.AuthStatus;
import ru.octoshell.bot.service.remote.wrappers.auth.AuthenticationService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AuthSettingsState implements State {

    private final BotLinkService botLinkService;
    private final AuthenticationService authenticationService;
    private final UserStateService userStateService;
    private final LocaleService localeService;

    public AuthSettingsState(BotLinkService botLinkService,
                             AuthenticationService authenticationService,
                             UserStateService userStateService,
                             LocaleService localeService) {
        this.botLinkService = botLinkService;
        this.authenticationService = authenticationService;
        this.userStateService = userStateService;
        this.localeService = localeService;
    }

    @Override
    public Pair<UserState, Reaction> transition(Update update) {
        String text = update.getText();
        if (!Objects.isNull(text)) {
            String locale = userStateService.getUserLocale(update.getUserId());
            Button button = Button.findByText(localeService, locale, text);

            if (Objects.nonNull(button)) {
                switch (button) {
                    case CHANGE_EMAIL:
                        return Pair.of(UserState.AUTH_NEW_EMAIL, null);
                    case CHANGE_TOKEN:
                        return Pair.of(UserState.AUTH_NEW_TOKEN, null);
                    case SHOW_SETTINGS:
                        return Pair.of(null, showSettings(locale, update));
                    case CHECK_CONNECTION:
                        return Pair.of(null, checkConnection(locale, update));
                    default:
                        return Pair.of(UserState.MAIN_MENU, null);
                }
            }
        }

        return null;
    }

    private Reaction showSettings(String locale, Update update) {
        Integer userId = update.getUserId();
        String email = StringUtils.defaultIfEmpty(botLinkService.getEmail(userId),
                localeService.get(locale, "auth.blank-email"));
        String token = StringUtils.defaultIfEmpty(botLinkService.getToken(userId),
                localeService.get(locale, "auth.blank-token"));

        StringBuilder sb = new StringBuilder();
        sb.append(localeService.get(locale, "auth.settings.header"))
                .append("\n");
        sb.append(localeService.get(locale, "auth.settings.email"))
                .append(": ").append(email).append("\n");
        sb.append(localeService.get(locale, "auth.settings.token"))
                .append(": ").append(token).append("\n");

        Reaction reaction = new Reaction();
        reaction.setText(sb.toString());
        return reaction;
    }

    private Reaction checkConnection(String locale, Update update) {
        Integer userId = update.getUserId();

        AuthStatus authStatus = authenticationService.authenticate(userId);

        StringBuilder sb = new StringBuilder();
        sb.append(localeService.get(locale, "auth.check.header")).append("\n");
        sb.append(localeService.get(locale, authStatus.getDescription()));

        Reaction reaction = new Reaction();
        reaction.setText(sb.toString());
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
        keyboard.add(buildRow(locale, Button.CHANGE_EMAIL, Button.CHANGE_TOKEN));
        keyboard.add(buildRow(locale, Button.SHOW_SETTINGS));
        keyboard.add(buildRow(locale, Button.CHECK_CONNECTION));
        keyboard.add(buildRow(locale, Button.BACK));

        Reaction reaction = new Reaction();
        reaction.setKeyboard(keyboard);
        reaction.setText(localeService.get(locale, "auth.message"));
        return reaction;
    }

    private enum Button {
        CHANGE_EMAIL("auth.button.change-email"),
        CHANGE_TOKEN("auth.button.change-token"),
        SHOW_SETTINGS("auth.button.show-settings"),
        CHECK_CONNECTION("auth.button.check-connection"),
        BACK("auth.button.back");

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
