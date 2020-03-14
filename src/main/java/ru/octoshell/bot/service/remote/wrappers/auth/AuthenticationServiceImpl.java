package ru.octoshell.bot.service.remote.wrappers.auth;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.service.remote.core.RemoteCommandsService;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final RemoteCommandsService remoteCommandsService;

    public AuthenticationServiceImpl(RemoteCommandsService remoteCommandsService) {
        this.remoteCommandsService = remoteCommandsService;
    }

    @Override
    public AuthStatus authenticate(Integer userId) {
        try {
            JSONObject response = new JSONObject(remoteCommandsService.sendWithAuth(userId, ImmutableMap.of("method", "auth")));
            return AuthStatus.findByCode(response.getInt("status"));
        } catch (Exception e) {
            log.error("Something wrong with authenticate()");
            log.error(e.toString());
            return AuthStatus.SERVICE_UNAVAILABLE;
        }
    }

}
