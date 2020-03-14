package ru.octoshell.bot.service.handler.botlink;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.model.BotLinkData;
import ru.octoshell.bot.model.repository.BotLinkDataRepository;

import java.util.Optional;

@Slf4j
@Service
public class BotLinkServiceImpl implements BotLinkService {

    private final BotLinkDataRepository botLinkDataRepository;

    public BotLinkServiceImpl(BotLinkDataRepository botLinkDataRepository) {
        this.botLinkDataRepository = botLinkDataRepository;
    }

    @Override
    public BotLinkData getBotLinkData(Integer userId) {
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

    @Override
    public void updateEmail(Integer userId, String email) {
        log.info("Set new email: user id = {}, email = {}", userId, email);
        BotLinkData botLinkData = getBotLinkData(userId);
        botLinkData.setEmail(email);
        botLinkDataRepository.save(botLinkData);
    }

    @Override
    public void updateToken(Integer userId, String token) {
        log.info("Set new token: user id = {}, token = {}", userId, token);
        BotLinkData botLinkData = getBotLinkData(userId);
        botLinkData.setToken(token);
        botLinkDataRepository.save(botLinkData);
    }

    @Override
    public String getEmail(Integer userId) {
        return getBotLinkData(userId).getEmail();
    }

    @Override
    public String getToken(Integer userId) {
        return getBotLinkData(userId).getToken();
    }
}
