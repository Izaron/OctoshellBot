package ru.octoshell.bot.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.model.BotLinkData;
import ru.octoshell.bot.model.repository.BotLinkDataRepository;
import ru.octoshell.bot.service.LocaleService;
import ru.octoshell.bot.service.OctoshellTelegramBot;
import ru.octoshell.bot.service.statemachine.UserStateService;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
public class WebController {

    private final BotLinkDataRepository botLinkDataRepository;
    private final OctoshellTelegramBot octoshellTelegramBot;
    private final UserStateService userStateService;
    private final LocaleService localeService;

    public WebController(BotLinkDataRepository botLinkDataRepository,
                         OctoshellTelegramBot octoshellTelegramBot,
                         UserStateService userStateService,
                         LocaleService localeService) {
        this.botLinkDataRepository = botLinkDataRepository;
        this.octoshellTelegramBot = octoshellTelegramBot;
        this.userStateService = userStateService;
        this.localeService = localeService;
    }

    @PostMapping("/notify/ticket")
    public void notifyTicket(@RequestBody Map<String, String> body) {
        new Thread(new Sender(body)).start();
    }

    private class Sender implements Runnable {

        private final Map<String, String> body;

        private Sender(Map<String, String> body) {
            this.body = body;
        }

        @Override
        public void run() {
            Optional<BotLinkData> botLinkDataOptional = botLinkDataRepository.findByEmail(body.get("email"));

            if (botLinkDataOptional.isPresent()) {
                BotLinkData botLinkData = botLinkDataOptional.get();
                Integer userId = botLinkData.getUserId();
                sendNotification(body.get("event"), body.get("subject"), userId);
            }

            log.info(body.toString());
        }

        private void sendNotification(String event, String ticketSubject, Integer userId) {
            String locale = userStateService.getUserLocale(userId);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(userId.toString());

            StringBuilder sb = new StringBuilder();
            sb.append(localeService.getProperty(locale, "notify.ticket.header")).append("\n");
            sb.append(localeService.getProperty(locale, "notify.ticket.subject"))
                    .append(": \"").append(ticketSubject).append("\"\n");
            sb.append(localeService.getProperty(locale, "notify.ticket.status." + event))
                    .append("\n");

            sendMessage.setText(sb.toString());

            try {
                octoshellTelegramBot.send(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.toString());
            }
        }
    }
}
