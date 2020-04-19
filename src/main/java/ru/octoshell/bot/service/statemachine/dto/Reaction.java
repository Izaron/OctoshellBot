package ru.octoshell.bot.service.statemachine.dto;

import lombok.Data;

import java.util.List;

@Data
public class Reaction {

    String text;
    List<List<String>> keyboard;
    boolean forceReply;
}
