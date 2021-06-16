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

        AtomicInteger cAcc = new AtomicInteger();
        AtomicInteger bAcc = new AtomicInteger();
        AtomicInteger dAcc = new AtomicInteger();
        AtomicInteger sAcc = new AtomicInteger();
        AtomicInteger pAcc = new AtomicInteger();

        turns.stream().sorted().forEach( turn -> {

            Salvo mySalvo = gamePlayer.getSalvos().stream().filter(s-> s.getTurn() == turn).findFirst().orElse(null);
            Salvo enemySalvo = enemy.getSalvos().stream().filter(s-> s.getTurn() == turn).findFirst().orElse(null);

            self.add(makeHits(turn,enemySalvo,myShips,cAcc,bAcc,dAcc,sAcc,pAcc));
            opponent.add(makeHits(turn,mySalvo,enemyShips,cAcc,bAcc,dAcc,sAcc,pAcc));

       });
        Map<String, Object> dto = Util.makeMap("self",self);
        dto.put("opponent",opponent);
        return dto;
    }

    private Map<String, Object> makeHits(long turn,Salvo salvo, Set<Ship> ships,
                                         AtomicInteger cAcc, AtomicInteger bAcc, AtomicInteger dAcc,
                                         AtomicInteger sAcc, AtomicInteger pAcc) {
        Map<String,Object> hitMap = Util.makeMap("turn",turn);
        List<String> hitLocations = getHitLocations(salvo,ships);
        hitMap.put("hitLocations", hitLocations);
        hitMap.put("damages",getDamages(hitLocations,ships,cAcc,bAcc,dAcc,sAcc,pAcc));

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

        //por cada salvolocation, recorrer los barcos y agregar a la lista las locaciones q coincidan con salvoLocation
        salvoLocation.stream().forEach( salvoL->{
            enemyShips.stream().forEach(ship -> {
                ship.getShipLocations().stream().forEach(shipL -> {
                        if(shipL.equals(salvoL))
                            hitLocations.add(salvoL);
                });
            });
        });

        return hitLocations;
    }

    private Map<String,Object> getDamages(List<String> hitLocations, Set<Ship> ships,
                                          AtomicInteger cAcc, AtomicInteger bAcc, AtomicInteger dAcc,
                                          AtomicInteger sAcc, AtomicInteger pAcc) {
        /* params:
            hitLocations: locations where a salvo hit a ship
            enemyShips: set of ships with its locations
            return List of ships hit
         */
        Map<String,Object> damage = new LinkedHashMap<>();

        List<Ship> carriers = ships.stream().filter(sh->sh.getType().toLowerCase().equals("carrier")).collect(Collectors.toList());
        List<Ship> battleships = ships.stream().filter(sh->sh.getType().toLowerCase().equals("battleship")).collect(Collectors.toList());
        List<Ship> destroyers = ships.stream().filter(sh->sh.getType().toLowerCase().equals("destroyer")).collect(Collectors.toList());
        List<Ship> submarines = ships.stream().filter(sh->sh.getType().toLowerCase().equals("submarine")).collect(Collectors.toList());
        List<Ship> patrolBoats = ships.stream().filter(sh->sh.getType().toLowerCase().equals("patrolboat")).collect(Collectors.toList());

        AtomicInteger cCounter = new AtomicInteger();
        AtomicInteger bCounter = new AtomicInteger();
        AtomicInteger dCounter = new AtomicInteger();
        AtomicInteger sCounter = new AtomicInteger();
        AtomicInteger pCounter = new AtomicInteger();

        hitLocations.stream().forEach( hit ->{

            if(carriers.stream().anyMatch(c-> c.getShipLocations().contains(hit))) {
                damage.put("carrierHits", cCounter.getAndIncrement());
                damage.put("carrier", cAcc.getAndIncrement());
            }else{
                damage.put("carrierHits", 0);
                damage.put("carrier", cAcc.get());
            }
            if(battleships.stream().anyMatch(c-> c.getShipLocations().contains(hit))){
                damage.put("battleshipHits", bCounter.getAndIncrement());
                damage.put("battleship", bAcc.getAndIncrement());
            }else{
                damage.put("battleshipHits", 0);
                damage.put("battleship", bAcc.get());
            }
            if(destroyers.stream().anyMatch(c-> c.getShipLocations().contains(hit))){
                damage.put("destroyerHits", dCounter.getAndIncrement());
                damage.put("destroyer", dAcc.getAndIncrement());
            }else{
                damage.put("destroyerHits", 0);
                damage.put("destroyer", dAcc.get());
            }
            if(submarines.stream().anyMatch(c-> c.getShipLocations().contains(hit))){
                damage.put("submarineHits", sCounter.getAndIncrement());
                damage.put("submarine", sAcc.getAndIncrement());
            }else{
                damage.put("submarineHits", 0);
                damage.put("submarine", sAcc.get());
            }
            if(patrolBoats.stream().anyMatch(c-> c.getShipLocations().contains(hit))){
                damage.put("patrolboatHits", pCounter.getAndIncrement());
                damage.put("patrolboat", pAcc.getAndIncrement());
            }else{
                damage.put("patrolboatHits", 0);
                damage.put("patrolboat", pAcc.get());
            }

        });

        /*
        ships.forEach(ship-> {

            switch(ship.getType().toLowerCase()){
                case "carrier":  ;break;
                case "battleship":  ;break;
                case "destroyer":  ;break;
                case "submarine":  ;break;
                case "patrolboat":  ;break;
            }
        });
         */
        return damage;
    }

}
