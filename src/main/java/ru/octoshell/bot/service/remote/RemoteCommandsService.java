package ru.octoshell.bot.service.remote;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.octoshell.bot.service.BotLinkService;
import ru.octoshell.bot.service.statemachine.UserStateService;

import java.util.HashMap;
import java.util.Map;

@Service
public class RemoteCommandsService {

    private final RestTemplate restTemplate;
    private final String remoteUrl;
    private final UserStateService userStateService;
    private final BotLinkService botLinkService;

    public RemoteCommandsService(RestTemplate restTemplate,
                                 @Value("${remote.url}") String remoteUrl,
                                 UserStateService userStateService,
                                 BotLinkService botLinkService) {
        this.restTemplate = restTemplate;
        this.remoteUrl = remoteUrl;
        this.userStateService = userStateService;
        this.botLinkService = botLinkService;
    }

    public JSONObject sendCommandWithAuth(Integer userId, Map<String, String> params) {
        String locale = userStateService.getUserLocale(userId);
        String email = StringUtils.defaultString(botLinkService.getEmail(userId));
        String token = StringUtils.defaultString(botLinkService.getToken(userId));

        Map<String, String> map = new HashMap<>(params);
        map.put("email", email);
        map.put("token", token);

        return sendCommandJson(map);
    }

    public String sendCommand(Map<String, ?> params) {
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

    public JSONObject sendCommandJson(Map<String, ?> params) {
        return new JSONObject(sendCommand(params));
    }

    public JSONArray sendCommandJsonArray(Map<String, ?> params) {
        return new JSONArray(sendCommand(params));
    }
}
