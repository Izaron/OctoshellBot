package ru.octoshell.bot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = "extra")
public class ExtraData {

    @Id
    Integer userId;
    Map<String, String> data;
}
