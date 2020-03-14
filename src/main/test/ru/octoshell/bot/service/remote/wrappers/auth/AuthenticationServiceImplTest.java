package ru.octoshell.bot.service.remote.wrappers.auth;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import ru.octoshell.bot.service.remote.core.RemoteCommandsService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;

class AuthenticationServiceImplTest {

    @Test
    void authenticate() {
        RemoteCommandsService remoteCommandsService = Mockito.mock(RemoteCommandsService.class);
        Mockito.when(remoteCommandsService.sendWithAuth(eq(2000), eq(ImmutableMap.of("method", "auth"))))
                .thenReturn("{\"status\": 0}");
        Mockito.when(remoteCommandsService.sendWithAuth(eq(2001), eq(ImmutableMap.of("method", "auth"))))
                .thenReturn("{\"status\": 1}");
        Mockito.when(remoteCommandsService.sendWithAuth(eq(2002), eq(ImmutableMap.of("method", "auth"))))
                .thenReturn("{\"status\": 2}");
        Mockito.when(remoteCommandsService.sendWithAuth(eq(2003), eq(ImmutableMap.of("method", "auth"))))
                .thenReturn("{\"status\": 3}");
        Mockito.when(remoteCommandsService.sendWithAuth(eq(2004), eq(ImmutableMap.of("method", "auth"))))
                .thenReturn("{\"status\": \"SOMETHING UNEXPECTED I'M SENDING TO YOU\"}");
        Mockito.when(remoteCommandsService.sendWithAuth(eq(2005), eq(ImmutableMap.of("method", "auth"))))
                .thenReturn("{\"where\": \"Moscow\"}");
        Mockito.when(remoteCommandsService.sendWithAuth(eq(2006), eq(ImmutableMap.of("method", "auth"))))
                .thenThrow(new HttpServerErrorException(HttpStatus.I_AM_A_TEAPOT));

        AuthenticationServiceImpl authenticationService = new AuthenticationServiceImpl(remoteCommandsService);

        assertSame(AuthStatus.SUCCESS, authenticationService.authenticate(2000));
        assertSame(AuthStatus.INACTIVE_TOKEN, authenticationService.authenticate(2001));
        assertSame(AuthStatus.WRONG_TOKEN, authenticationService.authenticate(2002));
        assertSame(AuthStatus.WRONG_EMAIL, authenticationService.authenticate(2003));
        assertSame(AuthStatus.SERVICE_UNAVAILABLE, authenticationService.authenticate(2004));
        assertSame(AuthStatus.SERVICE_UNAVAILABLE, authenticationService.authenticate(2005));
        assertSame(AuthStatus.SERVICE_UNAVAILABLE, authenticationService.authenticate(2006));
    }
}