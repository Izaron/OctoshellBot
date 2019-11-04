package ru.octoshell.bot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.model.BotLinkData;
import ru.octoshell.bot.model.repository.BotLinkDataRepository;

import java.util.Optional;

@Slf4j
@Service
public class BotLinkService {

    private final BotLinkDataRepository botLinkDataRepository;

    public BotLinkService(BotLinkDataRepository botLinkDataRepository) {
        this.botLinkDataRepository = botLinkDataRepository;
    }

    private BotLinkData getBotLinkData(Integer userId) {
        Optional<BotLinkData> botLinkDataOptional = botLinkDataRepository.findById(userId);
        if (!botLinkDataOptional.isPresent()) {
            BotLinkData botLinkData = new BotLinkData();
            botLinkData.setUserId(userId);
            botLinkDataRepository.save(botLinkData);

            return botLinkData;
        } else {
            return botLinkDataOptional.get();
        }
    }

    public void updateEmail(Integer userId, String email) {
        log.info("Set new email: user id = {}, email = {}", userId, email);
        BotLinkData botLinkData = getBotLinkData(userId);
        botLinkData.setEmail(email);
        botLinkDataRepository.save(botLinkData);
    }

    public void updateToken(Integer userId, String token) {
        log.info("Set new token: user id = {}, token = {}", userId, token);
        BotLinkData botLinkData = getBotLinkData(userId);
        botLinkData.setToken(token);
        botLinkDataRepository.save(botLinkData);
    }

    public String getEmail(Integer userId) {
        return getBotLinkData(userId).getEmail();
    }

    public String getToken(Integer userId) {
        return getBotLinkData(userId).getToken();
    }
}
