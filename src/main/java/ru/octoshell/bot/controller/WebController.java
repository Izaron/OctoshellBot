package ru.octoshell.bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.octoshell.bot.controller.notifiers.Notifier;
import ru.octoshell.bot.service.api.vk.VkApiWorker;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Обработчик запросов к боту из сторонних источников
 */
@Slf4j
@RestController
public class WebController {

    private final Notifier ticketNotifier;
    private final VkApiWorker vkApiWorker;

    public WebController(Notifier ticketNotifier,
                         VkApiWorker vkApiWorker) {
        this.ticketNotifier = ticketNotifier;
        this.vkApiWorker = vkApiWorker;
    }

    /**
     * Нотификация пользователя об изменении в тикете
     * @param body Тело запроса, протокол {'email': String, 'event': String, 'subject': String}
     */
    @PostMapping("/notify/ticket")
    public void notifyTicket(@RequestBody Map<String, String> body) {
        ticketNotifier.notify(body);
    }

    @GetMapping("/check")
    public String check() {
        return "Hello! Octoshell bot is working! Current time: " + LocalDateTime.now();
    }

    @PostMapping("/vk")
    public String vk(@RequestBody String body) {
        return vkApiWorker.work(body);
    }
}
