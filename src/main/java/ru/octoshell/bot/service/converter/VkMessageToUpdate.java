package ru.octoshell.bot.service.converter;

import com.vk.api.sdk.objects.messages.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.statemachine.dto.Update;

@Slf4j
@Service
public class VkMessageToUpdate implements Converter<Message, Update> {
    @Override
    public Update convert(Message message) {
        Update update = new Update();
        update.setText(message.getText());
        update.setUserId(message.getFromId());
        return update;
    }
}
