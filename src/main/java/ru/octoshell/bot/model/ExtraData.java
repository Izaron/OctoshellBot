package ru.octoshell.bot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * Связь данных о дополнительной информации пользователя с его идентификатором у соцсети
 */
@Data
@Document(collection = "extra")
public class ExtraData {

    /**
     * Идентификатор пользователя у соцсети
     */
    @Id
    Integer userId;

    /**
     * Любые данные, которые полезны во время работы (контекст пользователя)
     */
    Map<String, String> data;
}
