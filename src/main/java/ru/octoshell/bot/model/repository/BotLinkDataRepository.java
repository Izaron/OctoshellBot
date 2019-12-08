package ru.octoshell.bot.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.octoshell.bot.model.BotLinkData;

import java.util.Optional;

public interface BotLinkDataRepository extends MongoRepository<BotLinkData, Integer> {

    Optional<BotLinkData> findByEmail(String email);
}
