package ru.octoshell.bot.service.locale;

/**
 * Получение строки из языкового проекта по языку и идентификатору
 */
public interface LocaleService {

    String get(String locale, String key);

    static String getDefaultLocale() {
        return "en";
    }

    LocaleHandler buildHandler(String locale);

    interface LocaleHandler {
        String get(String key);
    }
}
