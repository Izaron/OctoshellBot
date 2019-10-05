package ru.octoshell.bot;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Log4j2
@Service
public class BotsApiService {

    private Thread botsApiThread;
    private OctoshellBot octoshellBot;

    public BotsApiService(OctoshellBot octoshellBot) {
        this.octoshellBot = octoshellBot;
    }

    @PostConstruct
    public void postConstruct() {
        botsApiThread = new Thread(new BotsApiRunnable());
        botsApiThread.start();
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Interrupt Bots Api Thread");
        botsApiThread.interrupt();
    }

    private class BotsApiRunnable implements Runnable {

        @Override
        public void run() {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

            try {
                telegramBotsApi.registerBot(octoshellBot);
            } catch (TelegramApiRequestException e) {
                log.error(e);
            }
        }
    }
}
