package ru.octoshell.bot.service;

import org.springframework.stereotype.Service;
import ru.octoshell.bot.model.ExtraData;
import ru.octoshell.bot.model.repository.ExtraDataRepository;

import java.util.HashMap;
import java.util.Optional;

@Service
public class ExtraDataService {

    private final ExtraDataRepository extraDataRepository;

    public ExtraDataService(ExtraDataRepository extraDataRepository) {
        this.extraDataRepository = extraDataRepository;
    }

    public void put(Integer userId, String key, String value) {
        ExtraData data = getExtraData(userId);
        data.getData().put(key, value);
        extraDataRepository.save(data);
    }

    public String get(Integer userId, String key) {
        ExtraData data = getExtraData(userId);
        return data.getData().getOrDefault(key, "");
    }

    private ExtraData getExtraData(Integer userId) {
        Optional<ExtraData> optionalExtraData = extraDataRepository.findById(userId);
        return optionalExtraData.orElseGet(() -> {
            ExtraData newData = new ExtraData();
            newData.setUserId(userId);
            newData.setData(new HashMap<>());
            return newData;
        });
    }
}
