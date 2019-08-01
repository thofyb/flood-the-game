package com.summerschool.flood.game.flood;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summerschool.flood.game.*;
import com.summerschool.flood.message.MakeActionMessage;
import com.summerschool.flood.server.GameService;
import com.summerschool.flood.server.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.summerschool.flood.game.GameStatus.*;

@Data
public class FloodGame implements IGame {

    private final static Logger LOG = LoggerFactory.getLogger(FloodGame.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private List<Player> players = new ArrayList<>();
    private Map<Player, Cell> playersStartPosition = new ConcurrentHashMap<>();
    private IFirstSearch firstSearch;
    private FloodState state;
    private String id;

    private int maxPlayers;

    public FloodGame(GameType type, int maxPlayersCount) {
        this.maxPlayers = maxPlayersCount;
        Field field = createField(type);
        this.state = new FloodState(field);
        this.firstSearch = new DepthFirstSearch(field);
        this.id = UUID.randomUUID().toString();

        LOG.info("Created game session UUID: {}\nGenerate field: \n{}", id, state.getField().getPrettyView());
    }

    private Field createField(GameType type) {
        switch (type) {
            case STANDARD:
                return new Field(10, 10);
            case FAST:
                return new Field(5, 5);
            default:
                throw new IllegalArgumentException("Unknown game type: " + type);
        }
    }

    @Override
    public synchronized void removePlayer(Player player) {
        players.remove(player);
        if (players.size() == 0) {
            this.state.setGameStatus(FINISHED);
        }
    }

    @Override
    public boolean matchType(Map<String, String> params) {
        return true;
    }

    @Override
    public boolean addPlayer(Player player) {
        if (players.size() < maxPlayers) {
            synchronized (this) {
                if (players.size() < maxPlayers) {
                    players.add(player);
                    if (players.size() == maxPlayers) {
                        this.state.setGameStatus(READY);
                        setPlayersStartPosition();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void makeAction(Player player, MakeActionMessage message) {
        FloodAction action = mapper.convertValue(message.getAction(), FloodAction.class);
        synchronized (this) {
            if (isValidAction(player, action)) {
                makeStep(action);
                LOG.info("Game session UUID: {}\nChange field: \n{}", id, state.getField().getPrettyView());
            } else {
                throw new ServiceException("Wrong action");
            }
        }
    }

    public boolean isValidAction(Player player, FloodAction action) {
        Field field = this.state.getField();

        Cell tmpCell = playersStartPosition.get(this.state.getNext());
        return player.equals(this.state.getNext()) &&
                tmpCell.getX() == action.getX() && tmpCell.getY() == action.getY() &&
                tmpCell.getColor() != action.getColor() &&
                field.isInternalAt(action.getX(), action.getY()) &&
                this.state.getGameStatus() == READY;
    }

    private void makeStep(FloodAction action) {
        firstSearch.start(action.getX(), action.getY(), action.getColor());

        if (state.getField().isFilledByOneColor()) {
            state.setWinner(this.state.getNext());
            state.setNext(null);
            state.setCell(null);
            state.setGameStatus(FINISHED);
        } else {
            int index = players.indexOf(state.getNext());
            Player next = players.get((index + 1) % players.size());
            state.setNext(next);
            state.setCell(playersStartPosition.get(next));
        }
    }

    public void setPlayersStartPosition() {
        Field field = state.getField();
        Player player = players.get(ThreadLocalRandom.current().nextInt(players.size()));

        playersStartPosition = new ConcurrentHashMap<>();
        playersStartPosition.put(players.get(0), field.getCells()[0][0]);
        //playersStartPosition.put(players.get(1), field.getCells()[field.getWidth() - 1][0]);
        //playersStartPosition.put(players.get(2), field.getCells()[field.getWidth() - 1][field.getHeight() - 1]);
        //playersStartPosition.put(players.get(3), field.getCells()[0][field.getHeight() - 1]);

        state.setNext(player);
        state.setCell(playersStartPosition.get(player));
    }

    @Override
    public boolean isReady() {
        return this.state.getGameStatus() == READY;
    }

    @Override
    public boolean isFinished() {
        return this.state.getGameStatus() == FINISHED;
    }

    @Data
    public class FloodState implements GameState {
        private Player next;
        private Player winner;
        private Field field;
        private Cell cell;
        private GameStatus gameStatus = NOT_READY;

        FloodState(Field field) {
            this.field = field;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FloodAction {
        private int x;
        private int y;
        private Color color;
    }
}
