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
            Mission default_mission = new Mission(
                    "The Midnight Masquerade Betrayal",
                    "A high-stakes mystery set in a secluded mountain mansion. Lord Blackwood has been found dead in the library during his own masquerade ball. Every guest is wearing a mask—both literally and figuratively.",
                    "Lord Julian Blackwood was poisoned during the toast at 00:00. The killer must be one of the two inner circle members present. " +
                            "1. The silent heir with gambling debts. 2. The personal doctor who was recently fired." +
                            "AI Instruction: Generate a timeline where each bot was seen near the drinks table. The AI must hide the fact that the wine bottle was swapped. " +
                            "Bots should provide conflicting accounts of who held the glass last." +
                            "Killer has very emotional reaction on Lord Blackwood and death was tough for Lord.",
                    2,
                    15
            );


            missionRepository.save(default_mission);
        }
    }
}
