package com.codeoftheweb.salvo.model;

import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER , cascade = CascadeType.ALL)
    private Set<Ship> ships = new HashSet<Ship>();



    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER , cascade = CascadeType.ALL)
    private Set<Salvo> salvos = new HashSet<Salvo>();

    private LocalDateTime date;

    public GamePlayer() {
    }

    public GamePlayer(Game game, Player player,LocalDateTime date) {
        this.player = player;
        this.game = game;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }


    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }

    @JsonIgnore
    public void addShip(Ship ship){
        ship.setGamePlayer(this);
        this.ships.add(ship);
    }

    @JsonIgnore
    public Set<Ship> getShips() {
        return ships;
    }

    @JsonIgnore
    public void addSalvo(Salvo salvo){
        salvo.setGamePlayer(this);
        this.salvos.add(salvo);
    }

    @JsonIgnore
    public Set<Salvo> getSalvos() {
        return salvos;
    }

    public Score getScore(Game game) {
        return this.getPlayer().getScore(game);
    }
}
