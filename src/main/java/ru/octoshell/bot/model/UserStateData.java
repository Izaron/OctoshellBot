package ru.octoshell.bot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "user_state")
public class UserStateData {

    @Id
    Integer userId;
    String state;
}