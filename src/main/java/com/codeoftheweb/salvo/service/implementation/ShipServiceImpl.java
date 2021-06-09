package com.codeoftheweb.salvo.service.implementation;

import com.codeoftheweb.salvo.model.Ship;
import com.codeoftheweb.salvo.service.ShipService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipServiceImpl implements ShipService {


    @Override
    public Ship savePlayer(Ship ship) {
        return null;
    }

    @Override
    public List<Ship> getPlayer() {
        return null;
    }

    @Override
    public Ship updatePlayer(Ship ship) {
        return null;
    }

    @Override
    public boolean existPlayer(Long id) {
        return false;
    }

    @Override
    public Ship findPlayerById(Long id) {
        return null;
    }

    @Override
    public List<Ship> findAll() {
        return null;
    }
}
