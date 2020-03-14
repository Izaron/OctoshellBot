package ru.octoshell.bot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Связь данных о текущем состоянии пользователя с его идентификатором у соцсети
 */
@Data
@Document(collection = "user_state")
public class UserStateData {

    /**
     * Идентификатор пользователя у соцсети
     */
    @Id
    Integer userId;

    /**
     * Имя текущего состояния (listener-а)
     */
    String state;

    /**
     * Локаль (язык) взаимодействия
     */
    String locale;
}