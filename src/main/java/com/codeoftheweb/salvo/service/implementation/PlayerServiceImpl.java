package com.codeoftheweb.salvo.service.implementation;

import com.codeoftheweb.salvo.model.Player;
import com.codeoftheweb.salvo.repository.PlayerRepository;
import com.codeoftheweb.salvo.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    PlayerRepository playerRepository;

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public List<Player> getPlayer() {
        return null;
    }

    @Override
    public Player updatePlayer(Player Player) {
        return null;
    }

    @Override
    public boolean existPlayer(Long id) {
        return false;
    }

    public Player findPlayerByUserName(String name) {
        return playerRepository.findByUserName(name);
    }

    @Override
    public List<Player> findAll() {
        return null;
    }

    @Override
    public Player findByUserName(String username) {
        return playerRepository.findByUserName(username);
    }

    @Override
    public Player findById(Long id) {
        return playerRepository.findById(id).get();
    }

}
