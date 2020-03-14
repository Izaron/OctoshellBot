package ru.octoshell.bot.service.handler.userstate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.octoshell.bot.model.UserStateData;
import ru.octoshell.bot.model.repository.UserStateDataRepository;
import ru.octoshell.bot.service.locale.LocaleService;
import ru.octoshell.bot.service.statemachine.UserState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@RunWith(SpringJUnit4ClassRunner.class)
@DataMongoTest
class UserStateTransistorServiceImplTest {

    @Autowired
    private UserStateDataRepository userStateDataRepository;
    private UserStateServiceImpl userStateService;

    @BeforeEach
    void beforeEach() {
        UserStateData data1 = new UserStateData();
        data1.setLocale("fr");
        data1.setState("DELETED_STATE");
        data1.setUserId(1937);

        UserStateData data2 = new UserStateData();
        data2.setLocale("ge");
        data2.setState(UserState.TICKET_CLUSTER_CHOOSE.name());
        data2.setUserId(1939);

        userStateDataRepository.save(data1);
        userStateDataRepository.save(data2);
        userStateService = new UserStateServiceImpl(userStateDataRepository);
    }

    @AfterEach
    void afterEach() {
        userStateDataRepository.deleteAll();
    }

    @Test
    void getUserState() {
        assertSame(UserState.getDefaultState(), userStateService.getUserState(1937));
        assertSame(UserState.TICKET_CLUSTER_CHOOSE, userStateService.getUserState(1939));
        assertSame(UserState.getDefaultState(), userStateService.getUserState(134235));
    }

    @Test
    void setUserState() {
        userStateService.setUserState(1939, UserState.AUTH_NEW_EMAIL);
        assertSame(UserState.AUTH_NEW_EMAIL, userStateService.getUserState(1939));

        userStateService.setUserState(99911, UserState.LOCALE_SETTINGS);
        assertSame(UserState.LOCALE_SETTINGS, userStateService.getUserState(99911));
    }

    @Test
    void getUserLocale() {
        assertEquals("fr", userStateService.getUserLocale(1937));
        assertEquals("ge", userStateService.getUserLocale(1939));
        assertEquals(LocaleService.getDefaultLocale(), userStateService.getUserLocale(1999));
    }

    @Test
    void setUserLocale() {
        userStateService.setUserLocale(1937, "us");
        assertEquals("us", userStateService.getUserLocale(1937));

        userStateService.setUserLocale(1999, "ja");
        assertEquals("ja", userStateService.getUserLocale(1999));
    }
}