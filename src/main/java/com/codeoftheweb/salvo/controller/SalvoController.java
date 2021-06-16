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

        Map<String,Object> hits = new LinkedHashMap<String,Object>();
        hits.put("self", new ArrayList<>());
        hits.put("opponent", new ArrayList<>());



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

        gpMap.put("hits", hits);

        return gpMap;
    }


    private Player getUser(Authentication authentication) {
        //receive an authentication object and get the player asociated

        if(authentication != null)
            return playerService.findByUserName(authentication.getName());
        else
            return null;
    }

    private Map<String, Object> shipsAfloat(GamePlayer gamePlayer){
       Map<String,Object> dto = Util.makeMap("","");
       List<?> hits = new ArrayList<Object>();

        GamePlayer enemy = gamePlayer.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayer.getId()).findFirst().orElse(null);
        Set<Long> turns = gamePlayer.getSalvos().stream().map(Salvo::getTurn).collect(Collectors.toSet());


        turns.stream().forEach( turn -> {


            Salvo mySalvo = gamePlayer.getSalvos().stream().filter(s-> s.getTurn() == turn).findFirst().orElse(null);
            Salvo enemySalvo = enemy.getSalvos().stream().filter(s-> s.getTurn() == turn).findFirst().orElse(null);

            Set<Ship> myShips = gamePlayer.getShips();
            Set<Ship> enemyShips = enemy.getShips();


            //a list of number of new hits and sinks for the player, including the types of ships involved
            if(mySalvo != null)
            {
                Map<String,Object> myHitMap = new HashMap<String,Object>();
                // enemy ships that are hit by my salvos
                //if my salvoLocations intersects with an enemy shipLocation then is a hit
                if(shipsHit(mySalvo,enemyShips)!=null){

                    myHitMap.put("hits",shipsHit(mySalvo,enemyShips).stream().map(sh->sh.getShipLocations()).collect(Collectors.toSet()));
                    myHitMap.put("type",shipsHit(mySalvo,enemyShips).stream().map(sh->sh.getType()).collect(Collectors.toSet()));

                    //if all of the shipLocations are hit then the ship is sunk
                    if(shipsSunked(mySalvo,enemyShips)!=null)
                    {
                        myHitMap.put("sinks",shipsSunked(mySalvo,enemyShips).stream().map(sh->sh.getShipLocations()).collect(Collectors.toSet()));
                    }
                }
            }
           //a similar list for the opponent

       });
        return dto;
    }

    private List<Ship> shipsSunked(Salvo salvo, Set<Ship> enemyShips) {
        /* params:
            salvo: Object salvo with its locations
            enemyShips: set of ships with its locations
            return List of ships sunked
         */

        return null;
    }

    private List<Ship> shipsHit(Salvo salvo, Set<Ship> enemyShips) {
        /* params:
            salvo: Object salvo with its locations
            enemyShips: set of ships with its locations
            return List of ships hit
         */
        /*
        Set<List<String>> shipLocations = enemyShips.stream().map(Ship::getShipLocations).collect(Collectors.toSet());
        List<String> salvoLocations = salvo.getSalvoLocations();

        shipLocations.stream().forEach( shipLocation-> {

        });
         */

        return enemyShips.stream().filter( ship->
                ship.getShipLocations().stream().anyMatch(shipL ->
                        salvo.getSalvoLocations().stream().anyMatch(salvoL ->
                                salvoL == shipL))).collect(Collectors.toList());
    }

}
