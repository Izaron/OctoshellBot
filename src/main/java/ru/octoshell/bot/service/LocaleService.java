package ru.octoshell.bot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
public class LocaleService {

    private Map<String, Properties> propMap = new HashMap<>();

    public static String getDefaultLocale() {
        return "en";
    }

    @PostConstruct
    public void postConstruct() {
        loadLanguagePack("ru");
        loadLanguagePack("en");
    }

    public String getProperty(String locale, String key) {
        Properties props = propMap.get(locale);
        try {
            return new String(props.getProperty(key, key).getBytes("ISO8859-1"));
        } catch (UnsupportedEncodingException e) {
            return key;
        }
    }

    private void loadLanguagePack(String lang) {
        Properties properties = new Properties();
        String filename = String.format("classpath:locales/%s.properties", lang);
        try {
            File file = ResourceUtils.getFile(filename);
            InputStream in = new FileInputStream(file);
            properties.load(in);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        propMap.put(lang, properties);
    }
}
