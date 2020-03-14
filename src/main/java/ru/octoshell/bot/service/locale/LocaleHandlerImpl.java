package ru.octoshell.bot.service.locale;

public class LocaleHandlerImpl implements LocaleService.LocaleHandler {

    private final LocaleService localeService;
    private final String locale;

    public LocaleHandlerImpl(LocaleService localeService, String locale) {
        this.localeService = localeService;
        this.locale = locale;
    }

    @Override
    public String get(String key) {
        return localeService.get(locale, key);
    }
}
