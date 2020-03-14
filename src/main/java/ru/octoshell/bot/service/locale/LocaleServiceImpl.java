package ru.octoshell.bot.service.locale;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Получение строки из языкового проекта по EN и RU из .properties
 */
@Slf4j
@Service
public class LocaleServiceImpl implements LocaleService {

    private final LocaleInputStreamService localeInputStreamService;

    private Map<String, Properties> propMap = new HashMap<>();

    public LocaleServiceImpl(LocaleInputStreamService localeInputStreamService) {
        this.localeInputStreamService = localeInputStreamService;
    }

    @PostConstruct
    public void postConstruct() {
        loadLanguagePack("ru");
        loadLanguagePack("en");
    }

    public String get(String locale, String key) {
        Properties props = propMap.get(locale);
        try {
            return new String(props.getProperty(key, key).getBytes("ISO8859-1"));
        } catch (UnsupportedEncodingException e) {
            return key;
        }
    }

    @Override
    public LocaleHandler buildHandler(String locale) {
        return new LocaleHandlerImpl(this, locale);
    }

    private void loadLanguagePack(String lang) {
        Properties properties = new Properties();
        try {
            properties.load(localeInputStreamService.getInputStream(lang));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        propMap.put(lang, properties);
    }
}
