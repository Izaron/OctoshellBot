package ru.octoshell.bot.service.handler.botlink;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.octoshell.bot.model.BotLinkData;
import ru.octoshell.bot.model.repository.BotLinkDataRepository;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DataMongoTest
class BotLinkServiceImplTest {

    private BotLinkData beforeData;

    @Autowired
    private BotLinkDataRepository botLinkDataRepository;
    private BotLinkServiceImpl botLinkService;

    @BeforeEach
    void beforeEach() {
        beforeData = new BotLinkData();
        beforeData.setEmail("pink@flamingo.com");
        beforeData.setToken("PINK_TOKEN");
        beforeData.setUserId(138);

        botLinkDataRepository.save(beforeData);
        botLinkService = new BotLinkServiceImpl(botLinkDataRepository);
    }

    @AfterEach
    void afterEach() {
        botLinkDataRepository.deleteAll();
    }

    @Test
    void getBotLinkData() {
        BotLinkData data1 = botLinkService.getBotLinkData(138);
        BotLinkData data2 = botLinkService.getBotLinkData(831);

        assertEquals(beforeData.getUserId(), data1.getUserId());
        assertEquals(beforeData.getEmail(), data1.getEmail());
        assertEquals(beforeData.getToken(), data1.getToken());

        assertEquals(831, (int)data2.getUserId());
        assertNull(data2.getEmail());
        assertNull(data2.getToken());
    }

    @Test
    void updateEmail() {
        botLinkService.updateEmail(138, "blue@pavian.net");
        botLinkService.updateEmail(831, "someone@somewhere.org");

        BotLinkData data1 = botLinkService.getBotLinkData(138);
        BotLinkData data2 = botLinkService.getBotLinkData(831);

        assertEquals(beforeData.getUserId(), data1.getUserId());
        assertEquals("blue@pavian.net", data1.getEmail());
        assertEquals(beforeData.getToken(), data1.getToken());

        assertEquals(831, (int)data2.getUserId());
        assertEquals("someone@somewhere.org", data2.getEmail());
        assertNull(data2.getToken());
    }

    @Test
    void updateToken() {
        botLinkService.updateToken(138, "NOT_OLD_TOKEN");
        botLinkService.updateToken(831, "TOKEN_OF_LIVE");

        BotLinkData data1 = botLinkService.getBotLinkData(138);
        BotLinkData data2 = botLinkService.getBotLinkData(831);

        assertEquals(beforeData.getUserId(), data1.getUserId());
        assertEquals(beforeData.getEmail(), data1.getEmail());
        assertEquals("NOT_OLD_TOKEN", data1.getToken());

        assertEquals(831, (int)data2.getUserId());
        assertNull(data2.getEmail());
        assertEquals("TOKEN_OF_LIVE", data2.getToken());
    }

    @Test
    void getEmail() {
        assertEquals(beforeData.getEmail(), botLinkService.getEmail(138));
        assertNull(botLinkService.getEmail(831));
    }

    @Test
    void getToken() {
        assertEquals(beforeData.getToken(), botLinkService.getToken(138));
        assertNull(botLinkService.getToken(831));
    }
}