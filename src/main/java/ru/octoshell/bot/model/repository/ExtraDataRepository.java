package ru.octoshell.bot.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.octoshell.bot.model.ExtraData;

public interface ExtraDataRepository extends MongoRepository<ExtraData, Integer> {
}
