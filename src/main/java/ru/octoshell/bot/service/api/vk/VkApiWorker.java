package ru.octoshell.bot.service.api.vk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vk.api.sdk.callback.CallbackApi;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.statemachine.StateMachineEngineService;
import ru.octoshell.bot.service.statemachine.dto.Reaction;
import ru.octoshell.bot.service.statemachine.dto.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Slf4j
@Service
public class VkApiWorker {

    private final String confirmationCode;
    private final CallbackApiHandler apiHandler;
    private final VkApiClient vk;
    private final GroupActor actor;

    private final StateMachineEngineService stateMachineEngineService;
    private final ConversionService conversionService;

    public VkApiWorker(@Value("${vk.group-id}") Integer groupId,
                       @Value("${vk.access-token}") String accessToken,
                       @Value("${vk.confirmation-code}") String confirmationCode,
                       StateMachineEngineService stateMachineEngineService,
                       ConversionService conversionService) {
        this.confirmationCode = confirmationCode;
        this.stateMachineEngineService = stateMachineEngineService;
        this.conversionService = conversionService;
        this.vk = new VkApiClient(HttpTransportClient.getInstance());
        this.actor = new GroupActor(groupId, accessToken);
        this.apiHandler = new CallbackApiHandler();
    }

    public String work(String body) {
        log.info("VKontakte request: " + body);

        // Catch confirmation
        JsonObject json = new Gson().fromJson(body, JsonObject.class);
        String type = json.get("type").getAsString();
        if (type.equalsIgnoreCase("confirmation")) {
            log.info("Need to confirm");
            return confirmationCode;
        }

        // Other types
        if (apiHandler.parse(json)) {
            return "ok";
        }
        return null;
    }

    public class CallbackApiHandler extends CallbackApi {

        private Random random = new Random();

        @Override
        public void messageNew(Integer groupId, Message message) {
            log.info("Got VKontakte message " + message);

            Update update = conversionService.convert(message, Update.class);
            List<Reaction> reactions = stateMachineEngineService.processUpdate(update);
            for (Reaction reaction : reactions) {
                applyReaction(reaction, update);
            }
        }

        private void applyReaction(Reaction reaction, Update update) {
            if (Objects.isNull(reaction)) {
                return;
            }

            MessagesSendQuery query = vk.messages().send(actor)
                    .peerId(update.getUserId())
                    .randomId(random.nextInt());

            // Set text
            String text = reaction.getText();
            if (Objects.nonNull(text)) {
                query.message(text);
            }

            // Set keyboard
            List<List<String>> keyboardTexts = reaction.getKeyboard();
            if (Objects.nonNull(keyboardTexts)) {
                Keyboard keyboard = new Keyboard();
                keyboard.setOneTime(false);
                keyboard.setInline(false);

                List<List<KeyboardButton>> buttons = new ArrayList<>();
                for (List<String> row : keyboardTexts) {
                    List<KeyboardButton> buttonsRow = new ArrayList<>();

                    for (String desc : row) {
                        KeyboardButtonAction action = new KeyboardButtonAction();
                        action.setType(KeyboardButtonActionType.TEXT);
                        action.setLabel(desc);

                        KeyboardButton button = new KeyboardButton();
                        button.setAction(action);

                        buttonsRow.add(button);
                    }
                    buttons.add(buttonsRow);
                }
                keyboard.setButtons(buttons);

                query.keyboard(keyboard);
            }

            // Set force reply
            // TODO: set force reply

            try {
                query.execute();
            } catch (ApiException | ClientException e) {
                e.printStackTrace();
            }
        }
    }
}
