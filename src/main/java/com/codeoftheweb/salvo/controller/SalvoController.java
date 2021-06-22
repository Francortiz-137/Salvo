package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.DTO.DTO;
import com.codeoftheweb.salvo.model.*;
import com.codeoftheweb.salvo.service.*;

import com.codeoftheweb.salvo.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameService gameService;
    @Autowired
    private GamePlayerService gamePlayerService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private ShipService shipService;
    @Autowired
    private SalvoService salvoService;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @RequestMapping("/games")
    public Map<String, Object> getGames(Authentication authentication)
    {
        /**authentication: logged-in user
         * returns all the games and the player that is logged in
         */
        Map<String, Object> gMap = new LinkedHashMap<String,Object>();
        List<Map<String, Object>> games = new ArrayList<>();


        gMap.put("games",gameService.findAll().stream()
                                .map(game -> DTO.gameToDTO(game))
                                .collect(toList()));
        if(!Util.isGuest(authentication)) {
            Map<String,Object> dtoPlayer = new LinkedHashMap<>();
            dtoPlayer.put("id", getUser(authentication).getId());
            dtoPlayer.put("email", getUser(authentication).getUserName());
            gMap.put("player", dtoPlayer);

        }else{
            gMap.put("player","Guest");
        }
        return gMap;
    }

    @RequestMapping("/game_view/{nn}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable Long nn, Authentication authentication){
        /** Return Map with a specific game information
        *   nn: Gameplayer Id
        *   autentication: log-in user
        **/

        if(Util.isGuest(authentication)) {
            return new ResponseEntity<Map<String, Object>>(Util.makeMap("error","Not Authorized"),HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> gpMap = new LinkedHashMap<String,Object>();
        GamePlayer gamePlayer = gamePlayerService.findById(nn);

        if(gamePlayer == null) {
            return new ResponseEntity<Map<String, Object>>(Util.makeMap("error","Not Authorized"),HttpStatus.UNAUTHORIZED);
        }

        if(gamePlayer.getPlayer().getId() != playerService.findByUserName(authentication.getName()).getId())
        {
            return new ResponseEntity<Map<String, Object>>(Util.makeMap("error","Not Authorized"),HttpStatus.UNAUTHORIZED);
        }

        gpMap = makeGameView(gamePlayer,gpMap);

        return new ResponseEntity<Map<String, Object>>(gpMap, HttpStatus.OK);
    }

    private Map<String, Object> makeGameView(GamePlayer gamePlayer, Map<String, Object> gpMap){
        //return a map of a determined game information
        Set<Ship> ships = gamePlayer.getShips();
        Game game = gamePlayer.getGame();
        Set<GamePlayer> players = game.getGamePlayers();
        Set<Salvo> salvos = players.stream().map(GamePlayer::getSalvos).flatMap(Collection::stream).collect(Collectors.toSet());

        //Game Information
        //gpMap = gameToDTO(gamePlayer.getGame());
        gpMap.put("id", game.getId());
        gpMap.put("created", game.getGameDate());
        gpMap.put("gamePlayers", game.getGamePlayers().stream()
                .map(gp -> DTO.gamePlayersToDTO(gp))
                .collect(toList()));

        //Ships Information
        gpMap.put("ships", ships.stream()
                .map(sh -> DTO.shipsToDTO(sh))
                .collect(toList()));

        //Salvos Information
        gpMap.put("salvoes", salvos.stream()
                .map(sal -> DTO.salvosToDTO(sal))
                .collect(toList()));

        //Hits
        Map<String, List<Map<String,Object>> > hits = hitsToDTO(gamePlayer);
        gpMap.put("hits", hits);

        //Game State
        gpMap.put("gameState", getGameState(gamePlayer,hits));

        return gpMap;
    }


    private Player getUser(Authentication authentication) {
        //receive an authentication object and get the player asociated

        if(authentication != null)
            return playerService.findByUserName(authentication.getName());
        else
            return null;
    }

    private Map<String, List<Map<String,Object>> > hitsToDTO(GamePlayer gamePlayer){
        // return a map with a list of hits for self and opponent
        List<Map<String,Object>> self = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> opponent = new ArrayList<Map<String,Object>>();

        GamePlayer enemy = gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayer.getId()).findFirst().orElse(new GamePlayer());

        Set<Long> turns = gamePlayer.getSalvos().stream().map(Salvo::getTurn).collect(Collectors.toSet());

        Set<Ship> myShips = gamePlayer.getShips();
        Set<Ship> enemyShips = enemy.getShips();

        Map<String,AtomicInteger> myHitAccumulator = createAccumulator();
        Map<String,AtomicInteger> enemyHitAccumulator = createAccumulator();

        Map<String, List<Map<String,Object>> > dto = new LinkedHashMap<String, List<Map<String,Object>>>();

        turns.stream().sorted().forEach( turn -> {
            Salvo mySalvo = gamePlayer.getSalvos().stream().filter(s-> s.getTurn() == turn).findFirst().orElse(new Salvo(null,turn, new ArrayList<>()));
            Salvo enemySalvo = enemy.getSalvos().stream().filter(s-> s.getTurn() == turn).findFirst().orElse(new Salvo(null,turn, new ArrayList<>()));

                self.add(makeHits(turn, enemySalvo, myShips, myHitAccumulator));
                opponent.add(makeHits(turn, mySalvo, enemyShips, enemyHitAccumulator));

       });


        dto.put("self",self);
        dto.put("opponent",opponent);
        return dto;
    }


    private Map<String, Object> makeHits(long turn,Salvo salvo, Set<Ship> ships, Map<String,AtomicInteger> hitAcc) {
        //return a map of hits with the turn, hitlocations, damages and missed shots

        Map<String,Object> hitMap = Util.makeMap("turn",turn);
        List<String> hitLocations = getHitLocations(salvo,ships);

        hitMap.put("hitLocations", hitLocations);
        hitMap.put("damages",getDamages(hitLocations,ships,hitAcc));

        int missedShots = salvo.getSalvoLocations().size() - hitLocations.size();
        hitMap.put("missed",missedShots);

        return hitMap;
    }

    private List<String> getHitLocations(Salvo salvo, Set<Ship> enemyShips) {
        /* params:
            salvo: Object salvo with its locations
            enemyShips: set of ships with its locations
            return List with the locations that match the salvos with the enemyships
         */
        List<String> hitLocations = new ArrayList<String>();
        if( salvo == null) return hitLocations;

        List<String> salvoLocation = salvo.getSalvoLocations();


        //for each salvolocation, loop the ships and search of the salvo location is in the list of locations of the ship
        salvoLocation.stream().forEach( salvoL->{
            enemyShips.stream().forEach(ship -> {
                if(ship.getShipLocations().contains(salvoL))
                            hitLocations.add(salvoL);
            });
        });
        return hitLocations;
    }

    private Map<String,Object> getDamages(List<String> hitLocations, Set<Ship> ships, Map<String,AtomicInteger> hitAcc) {
        /* params:
            hitLocations: locations where a salvo hit a ship
            enemyShips: set of ships with its locations
            return: List of ships hit
         */
        Map<String,Object> damage = new LinkedHashMap<>();

        List<String> carriers = getShipsBytype("carrier",ships);
        List<String> battleships = getShipsBytype("battleship",ships);
        List<String> destroyers = getShipsBytype("destroyer",ships);
        List<String> submarines = getShipsBytype("submarine",ships);
        List<String> patrolBoats = getShipsBytype("patrolboat",ships);

        Map<String, AtomicInteger> counter = createAccumulator();

        if(hitLocations.size()<=0){
            damage.put("carrierHits", counter.get("carrier").get());
            damage.put("battleshipHits", counter.get("battleship").get());
            damage.put("destroyerHits", counter.get("destroyer").get());
            damage.put("submarineHits", counter.get("submarine").get());
            damage.put("patrolboatHits", counter.get("patrolboat").get());
            damage.put("carrier", hitAcc.get("carrier").get());
            damage.put("battleship", hitAcc.get("battleship").get());
            damage.put("destroyer", hitAcc.get("destroyer").get());
            damage.put("submarine", hitAcc.get("submarine").get());
            damage.put("patrolboat", hitAcc.get("patrolboat").get());
        }

        //for each hit if the hit is in a certain type of ship then increment the counter for this turn
        // and the accumulated counter and add to the map
        // else add directly to the map without increment
        hitLocations.stream().forEach( hit ->{

            if(carriers.contains(hit)) {
                counter.get("carrier").incrementAndGet();
                hitAcc.get("carrier").incrementAndGet();
            }
            if(battleships.contains(hit)){
                counter.get("battleship").incrementAndGet();
                hitAcc.get("battleship").incrementAndGet();
            }
            if(destroyers.contains(hit)){
                counter.get("destroyer").incrementAndGet();
                hitAcc.get("destroyer").incrementAndGet();
            }
            if(submarines.contains(hit)){
                counter.get("submarine").incrementAndGet();
                hitAcc.get("submarine").incrementAndGet();
            }
            if(patrolBoats.contains(hit)){
                counter.get("patrolboat").incrementAndGet();
                hitAcc.get("patrolboat").incrementAndGet();
            }

            damage.put("carrierHits", counter.get("carrier").get());
            damage.put("battleshipHits", counter.get("battleship").get());
            damage.put("destroyerHits", counter.get("destroyer").get());
            damage.put("submarineHits", counter.get("submarine").get());
            damage.put("patrolboatHits", counter.get("patrolboat").get());
            damage.put("carrier", hitAcc.get("carrier").get());
            damage.put("battleship", hitAcc.get("battleship").get());
            damage.put("destroyer", hitAcc.get("destroyer").get());
            damage.put("submarine", hitAcc.get("submarine").get());
            damage.put("patrolboat", hitAcc.get("patrolboat").get());
        });

        return damage;
    }

    private List<String> getShipsBytype(String type, Set<Ship> ships) {
        //return a list with the locations of a ship given its type
        return ships.stream().filter(sh->sh.getType().toLowerCase().equals(type))
                .map(Ship::getShipLocations).flatMap(Collection::stream).collect(Collectors.toList());
    }


    private Map<String, AtomicInteger> createAccumulator() {
        //return a map with accumulators to count each ship number of hits

        Map<String,AtomicInteger> hitAccumulator = new HashMap<String,AtomicInteger>();
        hitAccumulator.put("carrier",new AtomicInteger());
        hitAccumulator.put("battleship",new AtomicInteger());
        hitAccumulator.put("destroyer",new AtomicInteger());
        hitAccumulator.put("submarine",new AtomicInteger());
        hitAccumulator.put("patrolboat",new AtomicInteger());

        return hitAccumulator;
    }

    private String getGameState(GamePlayer gamePlayer, Map<String, List<Map<String,Object>>> hits) {

        // Placeships is the first state by default when a player joins a match
        String state = "PLACESHIPS";
        GamePlayer opponent = getOpponent(gamePlayer);

        // Wait for a second player with ships
        if (gamePlayer.getShips().size() > 0 && (opponent.getShips().size() <= 0) && state == "PLACESHIPS"){
            state = "WAITINGFOROPP";
        }
        // play when ships already placed and the turn difference is at most 1
        if (opponent.getShips().size() > 0 && gamePlayer.getShips().size() > 0
                && gamePlayer.getSalvos().size() <= opponent.getSalvos().size()) {
            state = "PLAY";
        }

        //wait for salvoes (esperando salvos del oponente)
        if (gamePlayer.getSalvos().size() > opponent.getSalvos().size())
            state = "WAIT";


        boolean loseSelf = shipsSunk(gamePlayer, opponent);
        boolean loseOpp = shipsSunk(opponent, gamePlayer);
        //victory condition: all the opponent's ships are sunk, yours are not and both players are in the same turn
        if (!loseSelf && loseOpp && gamePlayer.getSalvos().size() == opponent.getSalvos().size()) {
            state = "WON";
            //add a victory to the score +1
            updateScore(gamePlayer, 1);
            updateScore(opponent, 0);
        }

        //both players ships were sunk in the same turn
        if (loseSelf && loseOpp && gamePlayer.getSalvos().size() == opponent.getSalvos().size()) {
            state = "TIE";
            //add a tie to the score +0.5
            updateScore(gamePlayer, 0.5);
            updateScore(opponent, 0.5);
        }
        // Your ships are all sunk but your opponent's not in the same turn
        if (loseSelf && !loseOpp && gamePlayer.getSalvos().size() == opponent.getSalvos().size()){
            state = "LOST";
            //add a lose to the score +0
            updateScore(gamePlayer, 0);
            updateScore(opponent, 1);
        }
        return state;
    }

    private void updateScore(GamePlayer gamePlayer, double points) {
        // when the game is over assign to the player and game the corresponding score

        Score newScore = new Score(gamePlayer.getPlayer(),gamePlayer.getGame(), LocalDateTime.now(),points);
        gamePlayer.getPlayer().addScore(newScore);
        gamePlayer.getGame().addScore(newScore);
        scoreService.saveScore(newScore);
    }

    private boolean shipsSunk(GamePlayer gamePlayer, GamePlayer opponent) {
        //return true if all of gameplayer ships are sunk

        if(!opponent.getShips().isEmpty() && !gamePlayer.getSalvos().isEmpty()){
            return opponent.getSalvos().stream().flatMap(salvo -> salvo.getSalvoLocations().stream()).collect(Collectors.toList()).containsAll(gamePlayer.getShips().stream()
                    .flatMap(ship -> ship.getShipLocations().stream()).collect(Collectors.toList()));
        }
        return false;
    }

    private GamePlayer getOpponent(GamePlayer gamePlayer) {
        //given a gamePlayer return the opponent in the same game
        return gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayer.getId()).findFirst().orElse(new GamePlayer());
    }

}
