package ru.octoshell.bot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZonedDateTime;
import java.util.Objects;

@Slf4j
@Service
public class OctoshellTelegramBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final int creatorId;

    protected OctoshellTelegramBot(@Value("${bot.token}") String botToken,
                                   @Value("${bot.username}") String botUsername,
                                   @Value("${bot.creator-id}") int creatorId,
                                   BotConnectionSettingsService options) {
        super(options);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.creatorId = creatorId;
    }

    private static String emojize(String text) {
        return EmojiParser.parseToUnicode(text);
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info(String.format("[%s] New update [%s] received at %s", this.botUsername, update.getUpdateId(), ZonedDateTime.now()));
        log.info(update.toString());
        long millisStarted = System.currentTimeMillis();

        Message message = update.getMessage();
        String text = message.getText();

        if (Objects.nonNull(text)) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(emojize(":pencil: You wrote: " + text));
            sendMessage.setChatId(message.getChatId().toString());
            try {
                sendApiMethod(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.toString());
            }
        }

        long processingTime = System.currentTimeMillis() - millisStarted;
        log.info(String.format("[%s] Processing of update [%s] ended at %s%n---> Processing time: [%d ms] <---%n", this.botUsername, update.getUpdateId(), ZonedDateTime.now(), processingTime));
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
