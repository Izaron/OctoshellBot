package ru.octoshell.bot.service.locale;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleServiceImplTest {

    @Test
    void get() {
        String english = "main.message=Press X \uD83D\uDD25 to win\n" +
                "submain.message=Press F ❤ to pay respect\n" +
                "who.i.am=\uD83C\uDF54 I am a man \uD83D\uDCA1\n";
        String russian = "main.message=Нажмите X \uD83D\uDD25 для победы\n" +
                "submain.message=Нажмите F ❤ чтобы отдать честь\n" +
                "who.i.am=\uD83C\uDF54 Я человек \uD83D\uDCA1\n";

        LocaleInputStreamService localeInputStreamService = Mockito.mock(LocaleInputStreamService.class);
        Mockito.when(localeInputStreamService.getInputStream("en")).thenReturn(new ByteArrayInputStream(english.getBytes()));
        Mockito.when(localeInputStreamService.getInputStream("ru")).thenReturn(new ByteArrayInputStream(russian.getBytes()));

        LocaleServiceImpl localeService = new LocaleServiceImpl(localeInputStreamService);
        localeService.postConstruct(); // necessary hack

        assertEquals("Press X \uD83D\uDD25 to win", localeService.get("en", "main.message"));
        assertEquals("Press F ❤ to pay respect", localeService.get("en", "submain.message"));
        assertEquals("\uD83C\uDF54 I am a man \uD83D\uDCA1", localeService.get("en", "who.i.am"));

        assertEquals("Нажмите X \uD83D\uDD25 для победы", localeService.get("ru", "main.message"));
        assertEquals("Нажмите F ❤ чтобы отдать честь", localeService.get("ru", "submain.message"));
        assertEquals("\uD83C\uDF54 Я человек \uD83D\uDCA1", localeService.get("ru", "who.i.am"));

        assertEquals("main.messagee", localeService.get("ru", "main.messagee"));
        assertEquals("main.mesage", localeService.get("en", "main.mesage"));
        assertThrows(NullPointerException.class, () -> localeService.get("fr", "main.message"));
    }

    @Test
    void getFromHandlers() {
        String english = "main.message=Press X \uD83D\uDD25 to win\n" +
                "submain.message=Press F ❤ to pay respect\n" +
                "who.i.am=\uD83C\uDF54 I am a man \uD83D\uDCA1\n";
        String russian = "main.message=Нажмите X \uD83D\uDD25 для победы\n" +
                "submain.message=Нажмите F ❤ чтобы отдать честь\n" +
                "who.i.am=\uD83C\uDF54 Я человек \uD83D\uDCA1\n";

        LocaleInputStreamService localeInputStreamService = Mockito.mock(LocaleInputStreamService.class);
        Mockito.when(localeInputStreamService.getInputStream("en")).thenReturn(new ByteArrayInputStream(english.getBytes()));
        Mockito.when(localeInputStreamService.getInputStream("ru")).thenReturn(new ByteArrayInputStream(russian.getBytes()));

        LocaleServiceImpl localeService = new LocaleServiceImpl(localeInputStreamService);
        localeService.postConstruct(); // necessary hack

        LocaleService.LocaleHandler ruHandler = localeService.buildHandler("ru");
        LocaleService.LocaleHandler enHandler = localeService.buildHandler("en");
        LocaleService.LocaleHandler frHandler = localeService.buildHandler("fr");

        assertEquals("Press X \uD83D\uDD25 to win", enHandler.get("main.message"));
        assertEquals("Press F ❤ to pay respect", enHandler.get("submain.message"));
        assertEquals("\uD83C\uDF54 I am a man \uD83D\uDCA1", enHandler.get("who.i.am"));

        assertEquals("Нажмите X \uD83D\uDD25 для победы", ruHandler.get("main.message"));
        assertEquals("Нажмите F ❤ чтобы отдать честь", ruHandler.get("submain.message"));
        assertEquals("\uD83C\uDF54 Я человек \uD83D\uDCA1", ruHandler.get("who.i.am"));

        assertEquals("main.messagee", ruHandler.get("main.messagee"));
        assertEquals("main.mesage", enHandler.get("main.mesage"));
        assertThrows(NullPointerException.class, () -> frHandler.get("main.message"));
    }
}