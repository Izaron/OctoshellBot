package ru.octoshell.bot.service.api.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.octoshell.bot.service.settings.TelegramSettingsService;
import ru.octoshell.bot.service.statemachine.StateMachineEngineService;
import ru.octoshell.bot.service.statemachine.dto.Reaction;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class TelegramApiWorkerBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;

    private final StateMachineEngineService stateMachineEngineService;
    private final ConversionService conversionService;

    protected TelegramApiWorkerBot(@Value("${telegram.token}") String botToken,
                                   @Value("${telegram.username}") String botUsername,
                                   TelegramSettingsService options,
                                   StateMachineEngineService stateMachineEngineService,
                                   ConversionService conversionService) {
        super(options);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.stateMachineEngineService = stateMachineEngineService;
        this.conversionService = conversionService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info(String.format("[%s] New update [%s] received at %s", this.botUsername, update.getUpdateId(), ZonedDateTime.now()));
        log.info(update.toString());
        long millisStarted = System.currentTimeMillis();

        ru.octoshell.bot.service.statemachine.dto.Update updateDto = conversionService
                .convert(update, ru.octoshell.bot.service.statemachine.dto.Update.class);

        // TODO: work with Reactions
        List<Reaction> reactions = stateMachineEngineService.processUpdate(updateDto);
        for (Reaction reaction : reactions) {
            applyReaction(reaction, updateDto);
        }

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

    private void applyReaction(Reaction reaction, ru.octoshell.bot.service.statemachine.dto.Update update) {
        if (Objects.isNull(reaction) || Objects.isNull(reaction.getText())) {
            return;
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getUserId().toString());

        // Set text
        sendMessage.setText(reaction.getText());

        // Set keyboard
        List<List<String>> keyboard = reaction.getKeyboard();
        if (Objects.nonNull(keyboard)) {
            List<KeyboardRow> keyboardRows = new ArrayList<>();
            for (List<String> row : keyboard) {
                KeyboardRow keyboardRow = new KeyboardRow();
                keyboardRow.addAll(row);
                keyboardRows.add(keyboardRow);
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(false);
            replyKeyboardMarkup.setKeyboard(keyboardRows);

            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }

        // Set force reply
        if (reaction.isForceReply()) {
            ForceReplyKeyboard replyKeyboard = new ForceReplyKeyboard();
            replyKeyboard.setSelective(false);
            sendMessage.setReplyMarkup(replyKeyboard);
        }

        try {
            this.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.toString());
        }
    }
}
