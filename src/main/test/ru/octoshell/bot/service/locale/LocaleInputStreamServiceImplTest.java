package ru.octoshell.bot.service.locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocaleInputStreamServiceImplTest {

    @Test
    void getInputStream() {
        LocaleInputStreamServiceImpl localeInputStreamService = new LocaleInputStreamServiceImpl();
        assertNotNull(localeInputStreamService.getInputStream("ru"));
        assertNotNull(localeInputStreamService.getInputStream("en"));
        assertNull(localeInputStreamService.getInputStream("ge"));
    }
}