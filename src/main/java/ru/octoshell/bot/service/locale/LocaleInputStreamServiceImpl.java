package ru.octoshell.bot.service.locale;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Создание InputStream для языкового пакета из ресурсов проекта в .properties
 */
@Slf4j
@Service
public class LocaleInputStreamServiceImpl implements LocaleInputStreamService {

    @Override
    public InputStream getInputStream(String lang) {
        String filename = String.format("classpath:locales/%s.properties", lang);
        try {
            InputStream in;
            if (isRunningFromJar()) {
                in = new ClassPathResource(filename).getInputStream();
            } else {
                File file = ResourceUtils.getFile(filename);
                in = new FileInputStream(file);
            }
            return in;
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private boolean isRunningFromJar() {
        String className = this.getClass().getName().replace('.', '/');
        String classJar = this.getClass().getResource("/" + className + ".class").toString();
        return classJar.startsWith("jar:");
    }
}
