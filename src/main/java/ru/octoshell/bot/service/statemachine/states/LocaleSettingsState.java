package ru.octoshell.bot.service.statemachine.states;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class LocaleSettingsState implements State {

    private final UserStateService userStateService;
    private final LocaleService localeService;

    public LocaleSettingsState(UserStateService userStateService,
                               LocaleService localeService) {
        this.userStateService = userStateService;
        this.localeService = localeService;
    }

    @Override
    public Pair<UserState, Reaction> transition(Update update) {
        String text = update.getText();
        if (!Objects.isNull(text)) {
            Integer userId = update.getUserId();
            String locale = userStateService.getUserLocale(userId);
            Button button = Button.findByText(localeService, locale, text);

            if (Objects.nonNull(button)) {
                switch (button) {
                    case RUSSIAN:
                        userStateService.setUserLocale(userId, "ru");
                        return Pair.of(UserState.MAIN_MENU, null);
                    case ENGLISH:
                        userStateService.setUserLocale(userId, "en");
                        return Pair.of(UserState.MAIN_MENU, null);
                    default:
                        return Pair.of(UserState.MAIN_MENU, null);
                }
            }
        }

        return null;
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
        keyboard.add(buildRow(locale, Button.RUSSIAN, Button.ENGLISH));

        Reaction reaction = new Reaction();
        reaction.setKeyboard(keyboard);
        reaction.setText(localeService.get(locale, "locale.message"));
        return reaction;
    }

    private enum Button {
        RUSSIAN("locale.button.russian"),
        ENGLISH("locale.button.english");

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
