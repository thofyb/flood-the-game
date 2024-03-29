package com.summerschool.flood;

import com.summerschool.flood.game.GameType;
import com.summerschool.flood.game.Player;

import com.summerschool.flood.game.flood.Color;
import com.summerschool.flood.game.flood.FloodGame;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class FloodGameTest {
    @Test
    public void testValidMakeStep() {
        FloodGame floodGame = new FloodGame(GameType.STANDARD, new HashMap<>());
        floodGame.setPlayers(new CopyOnWriteArrayList<>(Arrays.asList(
                new Player("0", "Zero", floodGame),
                new Player("1", "First", floodGame),
                new Player("2", "Second", floodGame),
                new Player("3", "Third", floodGame))));
        floodGame.setPlayersStartPosition();

        Player player = floodGame.getState().getNext();
        int x = floodGame.getState().getPositions().get(player).getX();
        int y = floodGame.getState().getPositions().get(player).getY();
        Color color = floodGame.getState().getField().getCells()[x][y].getColor();
        int k = ThreadLocalRandom.current().nextInt(Color.values().length);
        Color nextColor = Color.values()[k];

        for (int i = 0; i < Color.values().length && (nextColor == color || i == 0); i++)
            nextColor = Color.values()[(i + k) % Color.values().length];
        FloodGame.FloodAction floodGameActionMessage = new FloodGame.FloodAction(x, y, nextColor);
        assertTrue(floodGame.isValidAction(player, floodGameActionMessage));

    }
}
