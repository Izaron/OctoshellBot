package ru.octoshell.bot.service.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.model.UserStateData;
import ru.octoshell.bot.model.repository.UserStateDataRepository;
import ru.octoshell.bot.service.LocaleService;

import java.util.Optional;

@Slf4j
@Service
public class UserStateService {

    private final UserStateDataRepository userStateDataRepository;

    public UserStateService(UserStateDataRepository userStateDataRepository) {
        this.userStateDataRepository = userStateDataRepository;
    }

    public UserState getUserState(Integer userId) {
        UserStateData userStateData = getUserStateData(userId);
        String stateName = userStateData.getState();
        log.info("User state of user {} is '{}'", userId, stateName);
        return UserState.valueOf(stateName);
    }

    public void setUserState(Integer userId, UserState userState) {
        log.info("Save new user state: user = {}, user state = '{}'", userId, userState);

        UserStateData userStateData = getUserStateData(userId);
        userStateData.setState(userState.toString());
        userStateDataRepository.save(userStateData);
    }

    public String getUserLocale(Integer userId) {
        UserStateData userStateData = getUserStateData(userId);
        String locale = userStateData.getLocale();
        log.info("Locale of user {} is '{}'", userId, locale);
        return locale;
    }

    public void setUserLocale(Integer userId, String locale) {
        log.info("Save new user locale: user = {}, locale = '{}'", userId, locale);

        UserStateData userStateData = getUserStateData(userId);
        userStateData.setLocale(locale);
        userStateDataRepository.save(userStateData);
    }

    private UserStateData getUserStateData(Integer userId) {
        Optional<UserStateData> userStateDataOptional = userStateDataRepository.findById(userId);
        if (userStateDataOptional.isPresent()) {
            return userStateDataOptional.get();
        } else {
            UserState userState = UserState.getDefaultState();

            UserStateData userStateData = new UserStateData();
            userStateData.setUserId(userId);
            userStateData.setState(userState.name());
            userStateData.setLocale(LocaleService.getDefaultLocale());
            userStateDataRepository.save(userStateData);

            return userStateData;
        }
    }
}