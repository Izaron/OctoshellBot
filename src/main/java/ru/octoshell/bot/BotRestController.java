package ru.octoshell.bot;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@RestController
public class BotRestController {

    private AtomicInteger value = new AtomicInteger();

    @RequestMapping(value = "/value", method = RequestMethod.GET)
    public String getValue() {
        log.info("GET value");
        return StringUtils.join("The value is ", value);
    }

    @RequestMapping(value = "/value", method = RequestMethod.POST)
    public void postValue() {
        log.info("POST value");
        value.incrementAndGet();
    }
}
