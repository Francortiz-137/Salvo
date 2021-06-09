package com.codeoftheweb.salvo.service.implementation;

import com.codeoftheweb.salvo.model.Score;
import com.codeoftheweb.salvo.service.ScoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {
    @Override
    public Score saveScore(Score score) {
        return null;
    }

    @Override
    public List<Score> getScore() {
        return null;
    }

    @Override
    public Score updateScore(Score score) {
        return null;
    }

    @Override
    public boolean existScore(Long id) {
        return false;
    }

    @Override
    public Score findScoreById(Long id) {
        return null;
    }

    @Override
    public List<Score> findAll() {
        return null;
    }
}
