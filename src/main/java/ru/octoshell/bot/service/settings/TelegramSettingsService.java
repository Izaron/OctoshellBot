package ru.octoshell.bot.service.settings;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;

/**
 * Настройки Telegram из application.yml
 */
@Component
public class TelegramSettingsService extends DefaultBotOptions {

    private final Boolean useAuth;
    private final String authUser;
    private final String authPassword;

    public TelegramSettingsService(@Value("${telegram.use-proxy}") Boolean useProxy,
                                   @Value("${telegram.proxy.host}") String proxyHost,
                                   @Value("${telegram.proxy.port}") Integer proxyPort,
                                   @Value("${telegram.proxy.type}") String proxyType,
                                   @Value("${telegram.proxy.use-auth}") Boolean useAuth,
                                   @Value("${telegram.proxy.auth.user}") String authUser,
                                   @Value("${telegram.proxy.auth.password}") String authPassword) {
        this.useAuth = useAuth;
        this.authUser = authUser;
        this.authPassword = authPassword;

        if (BooleanUtils.isTrue(useProxy)) {
            setProxyHost(proxyHost);
            setProxyPort(proxyPort);
            setProxyType(DefaultBotOptions.ProxyType.valueOf(proxyType));
        }
    }

    public Boolean getUseAuth() {
        return useAuth;
    }

    public String getAuthUser() {
        return authUser;
    }

    public String getAuthPassword() {
        return authPassword;
    }
}
