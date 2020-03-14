package ru.octoshell.bot.service.handler.extra;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.octoshell.bot.model.ExtraData;
import ru.octoshell.bot.model.repository.ExtraDataRepository;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DataMongoTest
class ExtraDataServiceImplTest {

    private ExtraData beforeData;

    @Autowired
    private ExtraDataRepository extraDataRepository;
    private ExtraDataServiceImpl extraDataService;

    @BeforeEach
    void beforeEach() {
        beforeData = new ExtraData();
        beforeData.setData(Map.of("name", "Till", "surname", "Lindemann", "profession", "singer"));
        beforeData.setUserId(1937);

        extraDataRepository.save(beforeData);
        extraDataService = new ExtraDataServiceImpl(extraDataRepository);
    }

    @AfterEach
    void afterEach() {
        extraDataRepository.deleteAll();
    }

    @Test
    void put() {
        extraDataService.put(1937, "song", "Alter Mann");
        extraDataService.put(1937, "surname", "Kruspe");
        extraDataService.put(198, "who", "I am! \uD83D\uDE03");

        assertEquals("Till", extraDataService.get(1937, "name"));
        assertEquals("Kruspe", extraDataService.get(1937, "surname"));
        assertEquals("singer", extraDataService.get(1937, "profession"));
        assertEquals("Alter Mann", extraDataService.get(1937, "song"));
        assertEquals("I am! \uD83D\uDE03", extraDataService.get(198, "who"));

        assertEquals(StringUtils.EMPTY, extraDataService.get(1231414, "sjfsjkaf"));
        assertEquals(StringUtils.EMPTY, extraDataService.get(1938, "name"));
        assertEquals(StringUtils.EMPTY, extraDataService.get(1937, "surnname"));
        assertEquals(StringUtils.EMPTY, extraDataService.get(1937, "professio"));
        assertEquals(StringUtils.EMPTY, extraDataService.get(1937, "sonn"));
        assertEquals(StringUtils.EMPTY, extraDataService.get(198, "name"));
        assertEquals(StringUtils.EMPTY, extraDataService.get(198, "profession"));
    }

    @Test
    void get() {
        assertEquals("Till", extraDataService.get(1937, "name"));
        assertEquals("Lindemann", extraDataService.get(1937, "surname"));
        assertEquals("singer", extraDataService.get(1937, "profession"));

        assertEquals(StringUtils.EMPTY, extraDataService.get(1231414, "sjfsjkaf"));
        assertEquals(StringUtils.EMPTY, extraDataService.get(1938, "name"));
        assertEquals(StringUtils.EMPTY, extraDataService.get(1937, "surnname"));
        assertEquals(StringUtils.EMPTY, extraDataService.get(1937, "professio"));
    }
}