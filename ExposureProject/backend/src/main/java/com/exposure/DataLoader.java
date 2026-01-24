package com.exposure;

import com.exposure.models.Bot;
import com.exposure.models.Mission;
import com.exposure.models.User;
import com.exposure.repositories.BotRepository;
import com.exposure.repositories.MissionRepository;
import com.exposure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("test").isEmpty()) {
            User testUser = new User("test", "test");
            userRepository.save(testUser);
        }

        if (botRepository.count() == 0) {
            Bot botHarry = new Bot("Harry", "Very polite man");
            Bot botMax = new Bot("Max", "Very rude man");

            botRepository.save(botHarry);
            botRepository.save(botMax);
        }

        if (missionRepository.count() == 0) {
            Mission default_mission = new Mission("default_mission",
                    "mission about killing John",
                    "John very rich man. He stays in his house at party and got killed",
                    2);

            missionRepository.save(default_mission);
        }
    }
}
