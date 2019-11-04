package ru.octoshell.bot.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.octoshell.bot.model.BotLinkData;

public interface BotLinkDataRepository extends MongoRepository<BotLinkData, Integer> {
}