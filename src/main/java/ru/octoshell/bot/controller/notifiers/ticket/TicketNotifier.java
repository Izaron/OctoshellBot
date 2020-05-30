package ru.octoshell.bot.controller.notifiers.ticket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.controller.notifiers.Notifier;
import ru.octoshell.bot.model.BotLinkData;
import ru.octoshell.bot.model.repository.BotLinkDataRepository;
import ru.octoshell.bot.service.api.telegram.TelegramApiWorkerBot;
import ru.octoshell.bot.service.api.vk.VkApiWorker;
import ru.octoshell.bot.service.handler.userstate.UserStateService;
import ru.octoshell.bot.service.locale.LocaleService;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class TicketNotifier implements Notifier {

    private final BotLinkDataRepository botLinkDataRepository;
    private final TelegramApiWorkerBot telegramApiWorkerBot;
    private final VkApiWorker vkApiWorker;
    private final UserStateService userStateService;
    private final LocaleService localeService;

    public TicketNotifier(BotLinkDataRepository botLinkDataRepository, TelegramApiWorkerBot telegramApiWorkerBot,
                          VkApiWorker vkApiWorker, UserStateService userStateService, LocaleService localeService) {
        this.botLinkDataRepository = botLinkDataRepository;
        this.telegramApiWorkerBot = telegramApiWorkerBot;
        this.vkApiWorker = vkApiWorker;
        this.userStateService = userStateService;
        this.localeService = localeService;
    }

    @Override
    public void notify(Map<String, String> body) {
        new Thread(() -> run(body)).start();
    }

    private void run(Map<String, String> body) {
        Optional<BotLinkData> botLinkDataOptional = botLinkDataRepository.findByEmail(body.get("email"));

        if (botLinkDataOptional.isPresent()) {
            BotLinkData botLinkData = botLinkDataOptional.get();
            Integer userId = botLinkData.getUserId();
            sendNotification(body.get("event"), body.get("subject"), userId);
        }

        log.info(body.toString());
    }

    private void sendNotification(String event, String ticketSubject, Integer userId) {
        LocaleService.LocaleHandler handler = localeService.buildHandler(userStateService.getUserLocale(userId));

        String text = handler.get("notify.ticket.header") + "\n" +
                handler.get("notify.ticket.subject") + ": \"" + ticketSubject + "\"\n" +
                handler.get("notify.ticket.status." + event) + "\n";

        sendTelegram(userId, text);
        sendVk(userId, text);
    }

    private void sendTelegram(Integer userId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId.toString());
        sendMessage.setText(text);

        try {
            // wrong send is possible
            telegramApiWorkerBot.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }

    private void sendVk(Integer userId, String text) {
        // wrong send is possible
        vkApiWorker.sendMessage(userId, text);
    }
}
