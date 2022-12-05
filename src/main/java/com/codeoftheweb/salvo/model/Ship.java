package com.codeoftheweb.salvo.model;

import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name="locations")
    private List<String> shipLocations = new ArrayList<>();

    public Ship() {
    }

    public Ship(String type, GamePlayer gamePlayer, List<String> shipLocations) {
        this.type = type;
        this.gamePlayer = gamePlayer;
        this.shipLocations = shipLocations;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public List<String> getShipLocations() {
        return shipLocations;
    }

    public void setShipLocations(List<String> locations) {
        this.shipLocations = locations;
    }


    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }
}
