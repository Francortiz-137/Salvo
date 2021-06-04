package com.codeoftheweb.salvo;

import java.time.LocalDateTime;
import java.util.*;

import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import static java.util.stream.Collectors.toList;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private LocalDateTime gameDate;
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private List<GamePlayer> gamePlayers = new ArrayList<>();
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private List<Score> scores = new ArrayList<>();

    public Game() {
    }

    public Game(LocalDateTime gameDate) {
        this.gameDate = gameDate;
    }


    public long getId() {
        return id;
    }

    public LocalDateTime getGameDate() {
        return gameDate;
    }

    public void setGameDate(LocalDateTime gameDate) {
        this.gameDate = gameDate;
    }


    public List<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }


    public void addGamePlayers(GamePlayer gamePlayer) {
        gamePlayer.setGame(this);
        gamePlayers.add(gamePlayer);
    }

    public void addScore(Score score) {
        score.setGame(this);
        scores.add(score);
    }

}
