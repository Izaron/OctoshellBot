package ru.octoshell.bot.service.handler.extra;

/**
 * Связь сервисов с ExtraData
 */
public interface ExtraDataService {
    void put(Integer userId, String key, String value);

    String get(Integer userId, String key);
}
