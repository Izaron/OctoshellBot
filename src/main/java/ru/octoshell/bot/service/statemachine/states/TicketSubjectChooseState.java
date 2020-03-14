package ru.octoshell.bot.service.statemachine.states;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.service.handler.extra.ExtraDataService;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.statemachine.UserState;
import ru.octoshell.bot.service.handler.userstate.UserStateService;

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
    public UserState transition(OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            String text = message.getText();
            Integer userId = message.getFrom().getId();
            extraDataService.put(userId, "subject", text);

            return UserState.TICKET_MESSAGE_CHOOSE;
        }

        return null;
    }

    @Override
    public void explain(UserState userState, OctoshellTelegramBot bot, Update update) {
        Message message = update.getMessage();
        Integer userId = message.getFrom().getId();
        String locale = userStateService.getUserLocale(userId);

        SendMessage sendMessage = new SendMessage();

        ForceReplyKeyboard keyboard = new ForceReplyKeyboard();
        keyboard.setSelective(false);
        sendMessage.setReplyMarkup(keyboard);

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(localeService.get(locale, "main.tickets.subject.message"));

        try {
            bot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }
}
