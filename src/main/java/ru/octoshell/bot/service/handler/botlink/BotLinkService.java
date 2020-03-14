package ru.octoshell.bot.service.handler.botlink;

import ru.octoshell.bot.model.BotLinkData;

/**
 * Связь сервисов с BotLinkData
 */
public interface BotLinkService {
    BotLinkData getBotLinkData(Integer userId);

    void updateEmail(Integer userId, String email);

    void updateToken(Integer userId, String token);

    String getEmail(Integer userId);

    String getToken(Integer userId);
}
