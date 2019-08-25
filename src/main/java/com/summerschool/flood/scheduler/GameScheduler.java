package com.summerschool.flood.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summerschool.flood.game.IGame;
import com.summerschool.flood.handler.WebSocketGameHandler;
import com.summerschool.flood.message.GameStateMessage;
import com.summerschool.flood.message.MessageType;
import com.summerschool.flood.server.GameService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@AllArgsConstructor
@Component
public class GameScheduler implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(GameService.class);
    private final GameService gameService;
    private final WebSocketGameHandler gameHandler;
    private final ObjectMapper mapper;
    @Value("${flood.session.waitTime}")
    private int waitTime;

    @Override
    @Scheduled(fixedDelay = 10000)
    public void run() {
        final Instant currentTime = Instant.now();

        gameService.getGames()
                .stream()
                .filter(IGame::isNotReady)
                .filter(game -> isWaitingLong(game, currentTime))
                .forEach(game -> {
                    try {
                        game.start();
                        LOG.info("Game: {} ready", game.getId());
                        String readyMessage = mapper.writeValueAsString(new GameStateMessage(game.getState(), MessageType.GAME_READY));
                        gameHandler.sendToAll(game, readyMessage);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
    }

    private boolean isWaitingLong(IGame game, Instant currentTime) {
        return game.getPlayers().size() > 1 && Duration.between(game.getUpdateTime(), currentTime).getSeconds() >= waitTime;
    }

}
