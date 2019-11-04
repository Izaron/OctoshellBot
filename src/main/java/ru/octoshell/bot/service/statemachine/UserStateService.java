package ru.octoshell.bot.service.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.octoshell.bot.model.UserStateData;
import ru.octoshell.bot.model.repository.UserStateDataRepository;

import java.util.Optional;

@Slf4j
@Service
public class UserStateService {

    private final UserStateDataRepository userStateDataRepository;

    public UserStateService(UserStateDataRepository userStateDataRepository) {
        this.userStateDataRepository = userStateDataRepository;
    }

    public UserState getUserState(Integer userId) {
        Optional<UserStateData> userStateDataOptional = userStateDataRepository.findById(userId);

        if (userStateDataOptional.isPresent()) {
            String stateName = userStateDataOptional.get().getState();
            log.info("User state of user {} is '{}'", userId, stateName);
            return UserState.valueOf(stateName);
        } else {
            UserState userState = UserState.getDefaultState();

            UserStateData userStateData = new UserStateData();
            userStateData.setUserId(userId);
            userStateData.setState(userState.name());
            userStateDataRepository.save(userStateData);

            return userState;
        }
    }

    public void setUserState(Integer userId, UserState userState) {
        log.info("Save new user state: user = {}, user state = '{}'", userId, userState);

        UserStateData userStateData = new UserStateData();
        userStateData.setUserId(userId);
        userStateData.setState(userState.toString());
        userStateDataRepository.save(userStateData);
    }
}
