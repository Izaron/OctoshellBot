package ru.octoshell.bot.service.remote;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class AuthentificationService {

    private final RemoteCommandsService remoteCommandsService;

    public AuthentificationService(RemoteCommandsService remoteCommandsService) {
        this.remoteCommandsService = remoteCommandsService;
    }

    public AuthStatus authentificate(String email, String token) {
        Map<String, String> map = ImmutableMap.of(
                "method", "auth",
                "email", email,
                "token", token
        );

        try {
            JSONObject response = remoteCommandsService.sendCommandJson(map);
            return AuthStatus.findByCode(response.getInt("status"));
        } catch (Exception e) {
            log.error("Something wrong with authentificate()");
            log.error(e.toString());
            return AuthStatus.SERVICE_UNAVAILABLE;
        }
    }

    public enum AuthStatus {
        SUCCESS(0, "Успешная аутентификация!"),
        INACTIVE_TOKEN(1, "Введен неактивный токен"),
        WRONG_TOKEN(2, "Введен неверный токен"),
        WRONG_EMAIL(3, "Введен неверный email"),
        SERVICE_UNAVAILABLE(4, "Сервис временно недоступен");

        private final Integer code;
        private final String description;

        AuthStatus(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public static AuthStatus findByCode(int code) {
            for (AuthStatus status : values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }

            log.warn("Unknown AuthStatus code! What is {}?", code);
            return SERVICE_UNAVAILABLE;
        }

        public String getDescription() {
            return description;
        }

        public Integer getCode() {
            return code;
        }
    }
}
