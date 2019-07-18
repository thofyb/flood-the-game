package com.summerschool.flood.game;

import lombok.Data;

import java.util.List;

@Data
public class FloodGame implements IGame {

    private List<Player> players;
    private Field field;

    public FloodGame(GameType type) {

        switch (type) {
            case STANDARD:
                this.field = new Field(10, 10);
                return;

            case FAST:
                this.field = new Field(5, 5);
        }
    }

    @Override
    public Result makeAction(Action action) {
        return null;
    }

}

