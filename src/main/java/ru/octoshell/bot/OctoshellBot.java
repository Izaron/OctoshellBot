package ru.octoshell.bot;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Log4j2
@Service
public class OctoshellBot extends AbilityBot {

    @Value("${bot.creator-id}")
    private int creatorId;

    protected OctoshellBot(@Value("${bot.token}") String botToken,
                           @Value("${bot.username}") String botUsername,
                           OctoshellBotOptions options) {
        super(botToken, botUsername, options);
    }

    @Override
    public int creatorId() {
        return creatorId;
    }

    public Ability sayHelloWorld() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world!")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Hello world!", ctx.chatId()))
                .build();
    }
}
