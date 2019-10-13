package ru.octoshell.bot.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Slf4j
@Service
public class TelegramApiRunnableService {

    private final OctoshellTelegramBot octoshellTelegramBot;
    private final BotConnectionSettingsService botConnectionSettingsService;
    private Thread botThread;

    public TelegramApiRunnableService(OctoshellTelegramBot octoshellTelegramBot,
                                      BotConnectionSettingsService botConnectionSettingsService) {
        this.octoshellTelegramBot = octoshellTelegramBot;
        this.botConnectionSettingsService = botConnectionSettingsService;
    }

    @PostConstruct
    public void postConstruct() {
        botThread = new Thread(new TelegramBotsApiRunnable());
        botThread.start();
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Interrupt Bots Api Thread");
        botThread.interrupt();
    }

    private class TelegramBotsApiRunnable implements Runnable {

        @Override
        public void run() {
            setAuthIfNeeded();
            registerBot();
        }

        private void setAuthIfNeeded() {
            if (BooleanUtils.isTrue(botConnectionSettingsService.getUseAuth())) {
                String authUser = botConnectionSettingsService.getAuthUser();
                String authPassword = botConnectionSettingsService.getAuthPassword();

                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authUser,
                                authPassword.toCharArray());
                    }
                });
            }
        }

        private void registerBot() {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

            try {
                telegramBotsApi.registerBot(octoshellTelegramBot);
            } catch (TelegramApiRequestException e) {
                log.error(e.toString());
            }
        }
    }
}
