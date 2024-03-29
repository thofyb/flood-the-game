package com.summerschool.flood.game;

import com.summerschool.flood.game.flood.Cell;
import com.summerschool.flood.game.flood.Field;

import java.util.Map;

public interface GameState {

    /** @return List of Winner (if the game is finished) or null otherwise */
    Map<Player, PlayerStatus> getPlayersStatus();

    /** @return Next player supposed to make an action [could be null] */
    Player getNext();

    /** @return Current game status: to continue of finish the game*/
    GameStatus getGameStatus();

    /** @return Current game field state */
    Field getField();

    /** @return Players current positions on the field */
    Map<Player, Cell> getPositions();

}
