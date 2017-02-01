package org.str.webapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.str.backend.GameOfLifeService;
import org.str.backend.GameOfLifeServiceImpl;

@Configuration
public class WebConfig {

    @Bean
    public GameOfLifeService getGameOfLifeService() {
        return new GameOfLifeServiceImpl();
    }

}
