package ru.octoshell.bot.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.octoshell.bot.model.UserStateData;

public interface UserStateDataRepository extends MongoRepository<UserStateData, Integer> {
}
