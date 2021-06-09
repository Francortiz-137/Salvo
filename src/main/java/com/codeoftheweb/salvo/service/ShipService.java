package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.model.Player;
import com.codeoftheweb.salvo.model.Ship;

import java.util.List;

public interface ShipService {

    Ship savePlayer(Ship ship);

    List<Ship> getPlayer();

    Ship updatePlayer(Ship ship);

    boolean existPlayer(Long id);

    Ship findPlayerById(Long id);

    List<Ship> findAll();

}
