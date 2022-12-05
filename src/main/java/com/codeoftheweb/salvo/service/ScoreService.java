package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.model.Score;

import java.util.List;

public interface ScoreService {

    Score saveScore(Score score);

    List<Score> getScore();

    Score updateScore(Score score);

    boolean existScore(Long id);

    Score findScoreById(Long id);

    List<Score> findAll();

}
