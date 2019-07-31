package com.summerschool.flood.game.flood;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summerschool.flood.game.*;
import com.summerschool.flood.message.MakeActionMessage;
import com.summerschool.flood.server.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.summerschool.flood.game.GameStatus.FINISHED;
import static com.summerschool.flood.game.GameStatus.READY;

@Data
public class FloodGame implements IGame {

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
    }

    private Field createField(GameType type) {
        switch (type) {
            case STANDARD:
                return new Field(10, 10);
            case FAST:
                return new Field(5, 5);
            default:
                throw new IllegalArgumentException("Unknown game type = " + type);
        }
    }

    @Override
    public void removePlayer(Player player) {
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
                field.isInternalAt(action.getX(), action.getY());
    }

    public void makeStep(FloodAction action) {
        Cell tmpCell = new Cell(action.getX(), action.getY());
        tmpCell.setColor(action.getColor());
        firstSearch.start(tmpCell);
        int index = players.indexOf(this.state.getNext());
        state.setNext(this.players.get((index + 1) % players.size()));
    }

    public void setPlayersStartPosition() {
        Field field = this.state.getField();
        Player player = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        this.state.setNext(player);
        playersStartPosition = new ConcurrentHashMap<>();
        playersStartPosition.put(players.get(0), field.getCells()[0][0]);
        playersStartPosition.put(players.get(1), field.getCells()[field.getWidth() - 1][0]);
        playersStartPosition.put(players.get(2), field.getCells()[field.getWidth() - 1][field.getHeight() - 1]);
        playersStartPosition.put(players.get(3), field.getCells()[0][field.getHeight() - 1]);
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
    public class FloodState implements State {
        private Player next;
        private Field field;
        private GameStatus gameStatus;

        public FloodState(Field field) {
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

