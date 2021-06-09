package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.model.Game;
import java.util.List;


public interface GameService {

    Game saveGame(Game game);

    List<Game> getGame();

    Game updateGame(Game game);

    boolean existGame(Long id);

    Game findGameById(Long id);

    List<Game> findAll();

}
