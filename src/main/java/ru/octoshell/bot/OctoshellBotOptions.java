package ru.octoshell.bot;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Service
public class OctoshellBotOptions extends DefaultBotOptions {

    public OctoshellBotOptions(@Value("${bot.use-proxy}") Boolean useProxy,
                               @Value("${bot.proxy.host}") String proxyHost,
                               @Value("${bot.proxy.port}") Integer proxyPort,
                               @Value("${bot.proxy.type}") String proxyType) {
        if (BooleanUtils.isTrue(useProxy)) {
            setProxyHost(proxyHost);
            setProxyPort(proxyPort);
            setProxyType(DefaultBotOptions.ProxyType.valueOf(proxyType));
        }
    }
}
