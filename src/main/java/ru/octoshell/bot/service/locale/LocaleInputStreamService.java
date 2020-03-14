package ru.octoshell.bot.service.locale;

import java.io.InputStream;

/**
 * Создание InputStream для языкового пакета
 */
public interface LocaleInputStreamService {

    InputStream getInputStream(String lang);
}
