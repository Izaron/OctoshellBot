package ru.octoshell.bot.service.handler.extra;

import org.springframework.stereotype.Service;
import ru.octoshell.bot.model.ExtraData;
import ru.octoshell.bot.model.repository.ExtraDataRepository;

import java.util.HashMap;
import java.util.Optional;

@Service
public class ExtraDataServiceImpl implements ExtraDataService {

    private final ExtraDataRepository extraDataRepository;

    public ExtraDataServiceImpl(ExtraDataRepository extraDataRepository) {
        this.extraDataRepository = extraDataRepository;
    }

    @Override
    public void put(Integer userId, String key, String value) {
        ExtraData data = getExtraData(userId);
        data.getData().put(key, value);
        extraDataRepository.save(data);
    }

    @Override
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
