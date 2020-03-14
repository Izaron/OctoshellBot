package ru.octoshell.bot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Связь общих данных пользователя с его идентификатором у соцсети
 */
@Data
@Document(collection = "bot_links")
public class BotLinkData {

    /**
     * Идентификатор пользователя у соцсети
     */
    @Id
    Integer userId;

    /**
     * Токен, введенный пользователем
     */
    String token;

    /**
     * Email, введенный пользователем
     */
    String email;
}
