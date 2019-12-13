package ru.octoshell.bot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.service.statemachine.StateMachineService;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Slf4j
@Service
public class OctoshellTelegramBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final StateMachineService stateMachineService;

    protected OctoshellTelegramBot(@Value("${bot.token}") String botToken,
                                   @Value("${bot.username}") String botUsername,
                                   BotConnectionSettingsService options,
                                   StateMachineService stateMachineService) {
        super(options);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.stateMachineService = stateMachineService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info(String.format("[%s] New update [%s] received at %s", this.botUsername, update.getUpdateId(), ZonedDateTime.now()));
        log.info(update.toString());
        long millisStarted = System.currentTimeMillis();

        stateMachineService.processUpdate(this, update);

        long processingTime = System.currentTimeMillis() - millisStarted;
        log.info(String.format("[%s] Processing of update [%s] ended at %s%n---> Processing time: [%d ms] <---%n", this.botUsername, update.getUpdateId(), ZonedDateTime.now(), processingTime));
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> T send(Method method) throws TelegramApiException {
        return sendApiMethod(method);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
