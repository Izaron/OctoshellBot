package ru.octoshell.bot.service.remote.core;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.octoshell.bot.service.handler.botlink.BotLinkService;
import ru.octoshell.bot.service.handler.userstate.UserStateService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

class RemoteCommandsServiceImplTest {

    @Test
    void sendWithAuth() {
        // fix outer data
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        UserStateService userStateService = Mockito.mock(UserStateService.class);
        BotLinkService botLinkService = Mockito.mock(BotLinkService.class);
        String remoteUrl = "http://localhost:8080/ask/anything";
        int userId = 21341;

        Mockito.when(userStateService.getUserLocale(userId)).thenReturn("ru");
        Mockito.when(botLinkService.getEmail(userId)).thenReturn("me@myself.com");
        Mockito.when(botLinkService.getToken(userId)).thenReturn("SUPER_DUPER_TOKEN");

        // fix params and target
        Map<String, String> params = Map.of("day", "2020-02-29", "weather", "sunny");
        String answer = "Today is very sunny! ☀️ You need to get a walk \uD83D\uDEB6\u200D♂️ To the factory \uD83C\uDFED...";

        // fix answer
        String matcher = Mockito.argThat(s -> s.startsWith("http://localhost:8080/ask/anything?")
                && s.contains("weather=sunny")
                && s.contains("day=2020-02-29")
                && s.contains("email=me@myself.com")
                && s.contains("token=SUPER_DUPER_TOKEN"));
        Mockito.when(restTemplate.exchange(matcher, same(HttpMethod.GET), any(HttpEntity.class), same(String.class)))
                .thenReturn(new ResponseEntity<>(answer, HttpStatus.OK));

        RemoteCommandsServiceImpl remoteCommandsService = new RemoteCommandsServiceImpl(restTemplate, remoteUrl,
                userStateService, botLinkService);
        assertEquals(answer, remoteCommandsService.sendWithAuth(userId, params));
    }

    @Test
    void send() {
        // fix outer data
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        UserStateService userStateService = Mockito.mock(UserStateService.class);
        BotLinkService botLinkService = Mockito.mock(BotLinkService.class);
        String remoteUrl = "http://localhost:80/get/me";

        // fix params and target
        Map<String, String> params = Map.of("day", "2020-02-29", "weather", "sunny");
        String answer = "Today is very sunny! ☀️ You need to get a walk \uD83D\uDEB6\u200D♂️ To the factory \uD83C\uDFED...";

        // fix answer
        String matcher = Mockito.argThat(s -> s.startsWith("http://localhost:80/get/me?")
                && s.contains("weather=sunny")
                && s.contains("day=2020-02-29"));
        Mockito.when(restTemplate.exchange(matcher, same(HttpMethod.GET), any(HttpEntity.class), same(String.class)))
                .thenReturn(new ResponseEntity<>(answer, HttpStatus.OK));

        RemoteCommandsServiceImpl remoteCommandsService = new RemoteCommandsServiceImpl(restTemplate, remoteUrl,
                userStateService, botLinkService);
        assertEquals(answer, remoteCommandsService.send(params));
    }
}