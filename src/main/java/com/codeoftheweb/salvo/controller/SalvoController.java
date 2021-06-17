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
            dtoPlayer.put("email", getUser(authentication).getName());
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

        Set<Ship> ships = gamePlayer.getShips();
        Game game = gamePlayer.getGame();
        Set<GamePlayer> players = game.getGamePlayers();
        Set<Salvo> salvos = players.stream().map(GamePlayer::getSalvos).flatMap(Collection::stream).collect(Collectors.toSet());

        /*
        Map<String,Object> hits = new LinkedHashMap<String,Object>();
        hits.put("self", new ArrayList<>());
        hits.put("opponent", new ArrayList<>());
         */


        //Game Information
        //gpMap = gameToDTO(gamePlayer.getGame());
        gpMap.put("id", game.getId());
        gpMap.put("created", game.getGameDate());
        gpMap.put("gamePlayers", game.getGamePlayers().stream()
                .map(gp -> DTO.gamePlayersToDTO(gp))
                .collect(toList()));

        //Game State
        gpMap.put("gameState", "PLACESHIPS");
        //Ships Information
        gpMap.put("ships", ships.stream()
                .map(sh -> DTO.shipsToDTO(sh))
                .collect(toList()));

        //Salvos Information
        gpMap.put("salvoes", salvos.stream()
                .map(sal -> DTO.salvosToDTO(sal))
                .collect(toList()));

        gpMap.put("hits", hitsToDTO(gamePlayer));

        return gpMap;
    }


    private Player getUser(Authentication authentication) {
        //receive an authentication object and get the player asociated

        if(authentication != null)
            return playerService.findByUserName(authentication.getName());
        else
            return null;
    }

    private Map<String, Object> hitsToDTO(GamePlayer gamePlayer){

        List<Map<String,Object>> self = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> opponent = new ArrayList<Map<String,Object>>();

        GamePlayer enemy = gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayer.getId()).findFirst().orElse(null);
        Set<Long> turns = gamePlayer.getSalvos().stream().map(Salvo::getTurn).collect(Collectors.toSet());

        Set<Ship> myShips = gamePlayer.getShips();
        Set<Ship> enemyShips = enemy.getShips();

        Map<String,AtomicInteger> myHitAccumulator = createAccumulator();
        Map<String,AtomicInteger> enemyHitAccumulator = createAccumulator();

        turns.stream().sorted().forEach( turn -> {
            Salvo mySalvo = gamePlayer.getSalvos().stream().filter(s-> s.getTurn() == turn).findFirst().orElse(null);
            Salvo enemySalvo = enemy.getSalvos().stream().filter(s-> s.getTurn() == turn).findFirst().orElse(null);

            self.add(makeHits(turn,enemySalvo,myShips,myHitAccumulator));
            opponent.add(makeHits(turn,mySalvo,enemyShips,enemyHitAccumulator));
       });

        Map<String, Object> dto = Util.makeMap("self",self);
        dto.put("opponent",opponent);
        return dto;
    }


    private Map<String, Object> makeHits(long turn,Salvo salvo, Set<Ship> ships, Map<String,AtomicInteger> hitAcc) {
        Map<String,Object> hitMap = Util.makeMap("turn",turn);
        List<String> hitLocations = getHitLocations(salvo,ships);
        int missedShots = salvo.getSalvoLocations().size() - hitLocations.size();

        hitMap.put("hitLocations", hitLocations);
        hitMap.put("damages",getDamages(hitLocations,ships,hitAcc));
        hitMap.put("missed",missedShots);

        return hitMap;
    }

    private List<String> getHitLocations(Salvo salvo, Set<Ship> enemyShips) {
        /* params:
            salvo: Object salvo with its locations
            enemyShips: set of ships with its locations
            return List with the locations that match the salvos with the enemyships
         */

        List<String> salvoLocation = salvo.getSalvoLocations();
        List<String> hitLocations = new ArrayList<String>();

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
            return List of ships hit
         */
        Map<String,Object> damage = new LinkedHashMap<>();

        List<String> carriers = getShipsBytype("carrier",ships);
        List<String> battleships = getShipsBytype("battleship",ships);
        List<String> destroyers = getShipsBytype("destroyer",ships);
        List<String> submarines = getShipsBytype("submarine",ships);
        List<String> patrolBoats = getShipsBytype("patrolboat",ships);

        Map<String, AtomicInteger> counter = createAccumulator();

        //for each hit if the hit is in a certain type of ship then increment the counter for this turn and the accumulated counter and add to the map
        // else add directly to the map without increment
        hitLocations.stream().forEach( hit ->{

            if(carriers.contains(hit)) {
                damage.put("carrierHits", counter.get("carrier").incrementAndGet());
                damage.put("carrier", hitAcc.get("carrier").incrementAndGet());
            }else{
                damage.put("carrierHits", counter.get("carrier").get());
                damage.put("carrier", hitAcc.get("carrier").get());
            }
            if(battleships.contains(hit)){
                damage.put("battleshipHits", counter.get("battleship").incrementAndGet());
                damage.put("battleship", hitAcc.get("battleship").incrementAndGet());
            }else{
                damage.put("battleshipHits", counter.get("battleship").get());
                damage.put("battleship", hitAcc.get("battleship").get());
            }
            if(destroyers.contains(hit)){
                damage.put("destroyerHits", counter.get("destroyer").incrementAndGet());
                damage.put("destroyer", hitAcc.get("destroyer").incrementAndGet());
            }else{
                damage.put("destroyerHits", counter.get("destroyer").get());
                damage.put("destroyer", hitAcc.get("destroyer").get());
            }
            if(submarines.contains(hit)){
                damage.put("submarineHits", counter.get("submarine").incrementAndGet());
                damage.put("submarine", hitAcc.get("submarine").incrementAndGet());
            }else{
                damage.put("submarineHits", counter.get("submarine").get());
                damage.put("submarine", hitAcc.get("submarine").get());
            }
            if(patrolBoats.contains(hit)){
                damage.put("patrolboatHits", counter.get("patrolboat").incrementAndGet());
                damage.put("patrolboat", hitAcc.get("patrolboat").incrementAndGet());
            }else{
                damage.put("patrolboatHits", counter.get("patrolboat").get());
                damage.put("patrolboat", hitAcc.get("patrolboat").get());
            }
        });

        return damage;
    }

    private List<String> getShipsBytype(String type, Set<Ship> ships) {
        return ships.stream().filter(sh->sh.getType().toLowerCase().equals(type))
                .map(Ship::getShipLocations).flatMap(Collection::stream).collect(Collectors.toList());
    }


    private Map<String, AtomicInteger> createAccumulator() {

        Map<String,AtomicInteger> hitAccumulator = new HashMap<String,AtomicInteger>();
        hitAccumulator.put("carrier",new AtomicInteger());
        hitAccumulator.put("battleship",new AtomicInteger());
        hitAccumulator.put("destroyer",new AtomicInteger());
        hitAccumulator.put("submarine",new AtomicInteger());
        hitAccumulator.put("patrolboat",new AtomicInteger());

        return hitAccumulator;
    }

}
