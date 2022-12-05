package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.model.Player;

import java.util.List;

public interface PlayerService {

    Player savePlayer(Player player);

    List<Player> getPlayer();

    Player updatePlayer(Player player);

    boolean existPlayer(Long id);

    List<Player> findAll();

    Player findByUserName(String username);

    Player findById(Long id);
}
