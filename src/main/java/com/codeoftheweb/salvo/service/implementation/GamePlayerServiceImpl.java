package com.codeoftheweb.salvo.service.implementation;

import com.codeoftheweb.salvo.model.GamePlayer;
import com.codeoftheweb.salvo.repository.GamePlayerRepository;
import com.codeoftheweb.salvo.service.GamePlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GamePlayerServiceImpl implements GamePlayerService
{
    @Autowired
    GamePlayerRepository gamePlayerRepository;

    @Override
    public GamePlayer saveGamePlayer(GamePlayer gamePlayer) {
        return gamePlayerRepository.save(gamePlayer);
    }

    @Override
    public List<GamePlayer> getGamePlayer() {
        return null;
    }

    @Override
    public GamePlayer updateGamePlayer(GamePlayer gamePlayer) {
        return null;
    }

    @Override
    public boolean existGamePlayer(Long id) {
        return false;
    }

    @Override
    public GamePlayer findGamePlayerById(Long id) {
        if(gamePlayerRepository.findById(id).isPresent())
            return gamePlayerRepository.findById(id).get();
        else
            return null;
    }
}
