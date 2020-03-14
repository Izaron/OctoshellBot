package ru.octoshell.bot.service.remote.wrappers.auth;

/**
 * Проверка статуса аутентифицированности пользователя
 */
public interface AuthenticationService {
    AuthStatus authenticate(Integer userId);
}
