package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.model.Player;

import java.util.List;

public interface PlayerService {

    Player saveGame(Player game);

    List<Player> getGamePlayer();

    Player updateGame(Player player);

    boolean existPlayer(Long id);

    Player findPlayerById(Long id);

    List<Player> findAll();

}
