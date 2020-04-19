package ru.octoshell.bot.service.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.octoshell.bot.service.statemachine.dto.Update;

import java.util.Objects;

@Slf4j
@Service
public class TelegramUpdateToUpdate implements Converter<org.telegram.telegrambots.meta.api.objects.Update, Update> {
    @Override
    public Update convert(org.telegram.telegrambots.meta.api.objects.Update telegramUpdate) {
        Message message = telegramUpdate.getMessage();
        if (!message.isUserMessage()) {
            log.info("Is not user message!");
            return null;
        }

        User user = message.getFrom();
        if (Objects.isNull(user)) {
            log.info("Have no 'from' field in the message!");
            return null;
        }

        Update update = new Update();
        update.setUserId(user.getId());
        update.setText(message.getText());
        return update;
    }
}
