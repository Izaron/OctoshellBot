package ru.octoshell.bot.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.octoshell.bot.model.ExtraData;

/**
 * Работа с коллекцией (аналог таблицы в NoSQL БД) сущностей ExtraDataRepository
 */
public interface ExtraDataRepository extends MongoRepository<ExtraData, Integer> {
}
