package com.codeoftheweb.salvo.service.implementation;

import com.codeoftheweb.salvo.model.Ship;
import com.codeoftheweb.salvo.repository.ShipRepository;
import com.codeoftheweb.salvo.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipServiceImpl implements ShipService {

    @Autowired
    ShipRepository shipRepository;

    @Override
    public Ship saveShip(Ship ship) {
        return shipRepository.save(ship);
    }

    @Override
    public List<Ship> getShip() {
        return null;
    }

    @Override
    public Ship updateShip(Ship ship) {
        return null;
    }

    @Override
    public boolean existShip(Long id) {
        return false;
    }

    @Override
    public Ship findShipById(Long id) {
        return null;
    }

    @Override
    public List<Ship> findAll() {
        return null;
    }
}
