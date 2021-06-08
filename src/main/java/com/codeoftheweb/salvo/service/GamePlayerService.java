package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.model.GamePlayer;

import java.util.List;

public interface GamePlayerService {
    GamePlayer saveGamePlayer(GamePlayer gamePlayer);

    List<GamePlayer> getGamePlayer();

    GamePlayer updateGamePlayer(GamePlayer gamePlayer);

    boolean existGamePlayer(Long id);

    GamePlayer findGamePlayerById(Long id);
}
