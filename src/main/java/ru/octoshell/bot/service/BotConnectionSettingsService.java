package ru.octoshell.bot.service;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Component
public class BotConnectionSettingsService extends DefaultBotOptions {

    private final Boolean useAuth;
    private final String authUser;
    private final String authPassword;

    public BotConnectionSettingsService(@Value("${bot.use-proxy}") Boolean useProxy,
                                        @Value("${bot.proxy.host}") String proxyHost,
                                        @Value("${bot.proxy.port}") Integer proxyPort,
                                        @Value("${bot.proxy.type}") String proxyType,
                                        @Value("${bot.proxy.use-auth}") Boolean useAuth,
                                        @Value("${bot.proxy.auth.user}") String authUser,
                                        @Value("${bot.proxy.auth.password}") String authPassword) {
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
