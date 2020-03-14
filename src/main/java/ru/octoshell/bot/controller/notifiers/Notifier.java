package ru.octoshell.bot.controller.notifiers;

import java.util.Map;

public interface Notifier {
    void notify(Map<String, String> body);
}
