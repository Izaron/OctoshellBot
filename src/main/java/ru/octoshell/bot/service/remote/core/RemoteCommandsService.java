package ru.octoshell.bot.service.remote.core;

import java.util.Map;

/**
 * Точка входа для всех запросов к серверу Octoshell
 */
public interface RemoteCommandsService {
    String sendWithAuth(Integer userId, Map<String, String> params);

    String send(Map<String, ?> params);
}
