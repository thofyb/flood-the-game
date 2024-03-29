# Flood Project

Education project - client-server application, multi-player online
browser flood game with Java Spring-Boot based backend and JS phaser3
frontend.  

## Frameworks

* phaser3
* spring-boot

## Find game logic

Player could request server to find game with specified properties.
Flood game session could be started if 2 - 4 player were found.
If game does not have all the players (in our case it is 4 members), 
game will be started with its current players count. Wait time is 1 minute.
So, if players were waiting for new member more then 1 minute, then they
will start their game immediately.

## Communication interface

This section describes fully-specified client-server communication model 
with defined messages types and content. Also this section provides some 
messages examples as well as usage cases.

###  On connection (FROM Server TO Client)

After connection established and new player added into internal game service,
server sends message with player id and nickname to the client.
Note: nickname field could be null.

An example of message data.

```json
{
  "type":"playerInfo",
  "player": {
      "id":"3730b279-161c-50de-b0d2-7ad352032cb8",
      "nickname":null
  }
}
```

### Error message (FROM Server TO Client)

Server sends error messages of that format. 
There could be some useful info for player.

```json
{
  "type": "error",
  "time": "YYYY-MM-DD HH:MM:SS",
  "message": "Some very informative error text description" 
}
```

### Find new game session (FROM Client TO Server)

After connection established and if player has no active game you can ask for new game 
session for player. Send desired game type and options and server will: add to existing 
game session or create new empty game session and add player to that.

When game gets all players added, player will receive message with game state and its status READY.
After that players could make the actions.

```json
{
  "type": "findGame",
  "name": "flood",
  "gameType":"standard"
}
```

### Game ready (FROM Server TO Client)

Note: sent when game is ready to begin

Server send this message to the all players in current game session after game got READY. 
This message contains game state, which defines next player turn, winner, all players 
positions on the field with colors and all the field data with its properties.

Possible state's status: READY. 

```json
{
  "type": "gameReady",
  "state": {
      /** State foramt section */
  }
}
```

### Make game action (FROM Client TO Server)

Note: only for players with active game session

To make a valid action player must satisfy his turn and specify valid (x,y) position and color.
Server validates action and process it. Otherwise player receive error message if action is invalid.

```json
{
  "type": "makeAction",
  "action": {
      "x":1,
      "y":2,
      "color":"red"
  }
}

```

Note: all the valid colors are listed in the game.flood.Color enum

### Game state (FROM Server TO Client)

Note: sent when game in progress

Server send this message to the all players in current game session after each valid action. 
Contains all the game state data, needed to make next action.

Possible state's status: READY, FINISHED. 

After game finished all the players from that game are removed and marked as players, which
do not have active game. Therefore these players can request server to find new game.

```json
{
  "type": "gameState",
  "state": {
      /** State foramt section */
  }
}
```

### State property format (For game flood)

This section describes state property format, which contains all
the data about current game state: players, field, next, winner, etc.

Property 'positions' describes all the current players positions. Maps
players id to theirs cells. The next player position could be easily 
found from this list of data. 

Property 'playersStatus' describes current status of the all connected to the 
game players. It equals 'inGame' for all players when game started. If player 
lose he has 'loser' status. If player win the game he has 'winner' status. 

Note: if player lost, he cannot make actions any more, however he could watch how the 
game is processed. 

Note: property nickname could be null
Note: property next could be null (depends on the game status)
Note: property winner could be null (depends on the game status)

```json
{
  "state": {
    "next": {
      "id": "<Next turn action>",
      "nickname": "<Player optional nickname, could be null>"
    },
    "positions": {
          "070d40ed-9d7d-6efb-e420-d8e0a6e6fa20": {
            "x": 0,
            "y": 0,
            "color": "blue"
          },
          ...
          "123f40ef-1d9f-3adc-e333-cca0a0e6fa19": {
            "x": N,
            "y": N,
            "color": "red"
          }
    },
    "playersStatus": {
      "070d40ed-9d7d-6efb-e420-d8e0a6e6fa20": "winner",
      ...
      "123f40ef-1d9f-3adc-e333-cca0a0e6fa19": "loser",
      ...
      "3730b279-161c-50de-b0d2-7ad352032cb8": "inGame"
    },
    "field": {
      "cells": [
        [
          {
            "x": 0,
            "y": 0,
            "color": "<Some color, from color enum>"
          },
          ...
          {
            "x": width-1,
            "y": height-1,
            "color": "<Some color, from color enum>"
          }
        ]
      ],
      "width": 10,
      "height": 10
    },
    "gameStatus": "<Current game status>"
  }
}
```

### Message type enum

Note: case-sensitive

* error
* makeAction
* findGame
* gameReady
* gameState
* playerInfo

### Player status enum

* inGame
* winner
* loser

### Game status enum

* notReady
* ready
* finished

### Game type enum (For game flood)

* fast
* standard

### Colors enum (For game flood)

Note: case-sensitive

* red 
* green
* blue
* grey
* purple
* yellow
