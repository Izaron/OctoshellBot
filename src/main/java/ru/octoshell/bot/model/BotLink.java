package ru.octoshell.bot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "bot_links")
public class BotLink {

    @Id
    Long userId;
    String token;
    String email;
}
