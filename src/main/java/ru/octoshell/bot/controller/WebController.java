package ru.octoshell.bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.octoshell.bot.controller.notifiers.Notifier;

import java.util.Map;

/**
 * Обработчик запросов к боту из сторонних источников
 */
@Slf4j
@RestController
public class WebController {

    private final Notifier ticketNotifier;

    public WebController(Notifier ticketNotifier) {
        this.ticketNotifier = ticketNotifier;
    }

    /**
     * Нотификация пользователя об изменении в тикете
     * @param body Тело запроса, протокол {'email': String, 'event': String, 'subject': String}
     */
    @PostMapping("/notify/ticket")
    public void notifyTicket(@RequestBody Map<String, String> body) {
        ticketNotifier.notify(body);
    }

}
