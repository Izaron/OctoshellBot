package ru.octoshell.bot.service.remote.wrappers.auth;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum AuthStatus {
    SUCCESS(0, "auth.status.success"),
    INACTIVE_TOKEN(1, "auth.status.inactive-token"),
    WRONG_TOKEN(2, "auth.status.wrong-token"),
    WRONG_EMAIL(3, "auth.status.wrong-email"),
    SERVICE_UNAVAILABLE(4, "auth.status.service-unavailable");

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
