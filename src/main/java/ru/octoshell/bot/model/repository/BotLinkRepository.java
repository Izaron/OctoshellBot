package ru.octoshell.bot.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.octoshell.bot.model.BotLink;

public interface BotLinkRepository extends MongoRepository<BotLink, Long> {
}
