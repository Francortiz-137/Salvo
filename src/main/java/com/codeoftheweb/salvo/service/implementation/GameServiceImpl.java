package com.codeoftheweb.salvo.service.implementation;

import com.codeoftheweb.salvo.model.Game;
import com.codeoftheweb.salvo.repository.GameRepository;
import com.codeoftheweb.salvo.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    GameRepository gameRepository;

    @Override
    public Game saveGame(Game game) {
        gameRepository.save(game);
        return game;
    }

    @Override
    public List<Game> getGame() {
        return null;
    }

    @Override
    public Game updateGame(Game game) {
        return null;
    }

    @Override
    public boolean existGame(Long id) {
        return false;
    }

    @Override
    public Game findGameById(Long id) {
        return gameRepository.findGameById(id);
    }

    @Override
    public List<Game> findAll() {
        return gameRepository.findAll();
    }
}
