package com.codeoftheweb.salvo;

import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String userName;
    private String name;
    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private List<GamePlayer> gamesPlayed = new ArrayList<>();

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private List<Score> scores = new ArrayList<>();

    public Player() { }

    public Player(String name, String userName) {
        this.userName = userName;
        this.name = name;
    }


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String toString() {
        return userName;
    }

    @JsonIgnore
    public List<GamePlayer> getGames() {
        return gamesPlayed;
    }

    public void addGamePlayer(GamePlayer gameplayer) {
        gameplayer.setPlayer(this);
        gamesPlayed.add(gameplayer);
    }

    public void addScore(Score score) {
       score.setPlayer(this);
       scores.add(score);
    }

    public Score getScore(Game game) {
        return scores.stream()
                    .filter(p -> p.getGame().equals(game))
                    .findFirst()
                    .orElse(null);
    }
}
