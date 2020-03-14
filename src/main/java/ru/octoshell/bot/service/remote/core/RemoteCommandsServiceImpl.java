package ru.octoshell.bot.service.remote.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.octoshell.bot.service.handler.botlink.BotLinkService;
import ru.octoshell.bot.service.handler.userstate.UserStateService;

import java.util.HashMap;
import java.util.Map;

@Service
public class RemoteCommandsServiceImpl implements RemoteCommandsService {

    private final RestTemplate restTemplate;
    private final String remoteUrl;
    private final UserStateService userStateService;
    private final BotLinkService botLinkService;

    public RemoteCommandsServiceImpl(RestTemplate restTemplate,
                                     @Value("${remote.url}") String remoteUrl,
                                     UserStateService userStateService,
                                     BotLinkService botLinkService) {
        this.restTemplate = restTemplate;
        this.remoteUrl = remoteUrl;
        this.userStateService = userStateService;
        this.botLinkService = botLinkService;
    }

    @Override
    public String sendWithAuth(Integer userId, Map<String, String> params) {
        String locale = userStateService.getUserLocale(userId);
        String email = StringUtils.defaultString(botLinkService.getEmail(userId));
        String token = StringUtils.defaultString(botLinkService.getToken(userId));

        Map<String, String> map = new HashMap<>(params);
        map.put("email", email);
        map.put("token", token);

        return send(map);
    }

    @Override
    public String send(Map<String, ?> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(remoteUrl);
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            builder = builder.queryParam(entry.getKey(), entry.getValue());
        }

        HttpEntity<?> entity = new HttpEntity<>(headers);

        HttpEntity<String> response = restTemplate.exchange(
                builder.build().toUriString(),
                HttpMethod.GET,
                entity,
                String.class);

        return response.getBody();
    }

}
