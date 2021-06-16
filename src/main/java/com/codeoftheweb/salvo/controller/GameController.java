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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class GameController {

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


    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String email, @RequestParam String password) {

        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(Util.makeMap("error","Missing data"), HttpStatus.FORBIDDEN);
        }

        if (playerService.findByUserName(email) !=  null) {
            return new ResponseEntity<>(Util.makeMap("error","Name already in use"), HttpStatus.FORBIDDEN);
        }

        playerService.savePlayer(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Object> addGame(Authentication authentication){
        if(Util.isGuest(authentication)){
            return new ResponseEntity<>(Util.makeMap("error","Not Authorized"), HttpStatus.UNAUTHORIZED);
        }else {
            Game game = gameService.saveGame(new Game(LocalDateTime.now()));
            GamePlayer gamePlayer = gamePlayerService.saveGamePlayer(new GamePlayer(game, playerService.findByUserName(authentication.getName()),LocalDateTime.now()));
            return new ResponseEntity<>((Util.makeMap("gpid",gamePlayer.getId())),HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/game/{nn}/players", method = RequestMethod.POST)
    public ResponseEntity<Object> joinGame(@PathVariable Long nn, Authentication authentication){
        if(Util.isGuest(authentication)){
            return new ResponseEntity<>(Util.makeMap("error","Not Authorized"), HttpStatus.UNAUTHORIZED);
        }else {
            Game game = gameService.findById(nn);
            if (game == null) {
                return new ResponseEntity<>(Util.makeMap("error","No Such game"), HttpStatus.FORBIDDEN);
            }

            long n_players = game.getGamePlayers().size();
            if( n_players>1){
                return new ResponseEntity<>(Util.makeMap("error","Game is full"), HttpStatus.FORBIDDEN);
            }

            if(game.getGamePlayers().stream().filter(
                    gp-> gp.getPlayer().getId() == playerService.findByUserName(authentication.getName()).getId()).count() != 0 ){
                return new ResponseEntity<>(Util.makeMap("error","Player already in game"), HttpStatus.FORBIDDEN);
            }

            GamePlayer gamePlayer = gamePlayerService.saveGamePlayer(new GamePlayer(game,
                    playerService.findByUserName(authentication.getName()),
                    LocalDateTime.now()));
            return new ResponseEntity<>((Util.makeMap("gpid",gamePlayer.getId())),HttpStatus.CREATED);
        }
    }

    @PostMapping("/games/players/{gpid}/ships")
    public ResponseEntity<Object> saveShips(@PathVariable Long gpid, @RequestBody Set<Ship> ships, Authentication authentication){

        //validate if is logged in
        if(Util.isGuest(authentication)) {
            return new ResponseEntity<>(Util.makeMap("error", "Not Authorized"), HttpStatus.UNAUTHORIZED);
        }

        GamePlayer gamePlayer = gamePlayerService.findById(gpid);
        //validate if there is a gameplayer with the given ID
        if (gamePlayer == null) {
            return new ResponseEntity<>(Util.makeMap("error","Not Authorized"), HttpStatus.UNAUTHORIZED);
        }

        //validate if the current user is the same as the gameplayer given
        if(gamePlayer.getPlayer().getId() != playerService.findByUserName(authentication.getName()).getId()){
            return new ResponseEntity<>(Util.makeMap("error","Not Authorized"), HttpStatus.UNAUTHORIZED);
        }

        //validate if the player dont have previously any ship
        if(!gamePlayer.getShips().isEmpty()){
            return new ResponseEntity<>(Util.makeMap("error","Ships already placed"), HttpStatus.FORBIDDEN);
        }

        // validate correct number of cells for ship
        if(ships.stream().filter(ship -> ship.getShipLocations().size()>5).count() > 0 ) {
            return new ResponseEntity<>(Util.makeMap("error","Invalid ship size"), HttpStatus.FORBIDDEN);
        }

        //TODO validate ship size for type

        //validate max number of ships == 5
        if(ships.size() > 5){
            return new ResponseEntity<>(Util.makeMap("error","Max number of ships reached"), HttpStatus.FORBIDDEN);
        }

        // save each ship and add it to the gameplayer
        ships.stream().forEach((ship)->{

            gamePlayer.addShip(shipService.saveShip(ship));
        });

        return new ResponseEntity<>((Util.makeMap("OK","Ship Created")),HttpStatus.CREATED);
    }

    @PostMapping("/games/players/{gamePlayerId}/salvos")
    public ResponseEntity<Object> saveSalvoes(@PathVariable Long gamePlayerId, @RequestBody Salvo salvo, Authentication authentication){

        //validate if is logged in
        if(Util.isGuest(authentication)) {
            return new ResponseEntity<>(Util.makeMap("error", "Not Authorized"), HttpStatus.UNAUTHORIZED);
        }

        GamePlayer gamePlayer = gamePlayerService.findById(gamePlayerId);
        //validate if there is a gamePlayer with the given ID
        if (gamePlayer == null) {
            return new ResponseEntity<>(Util.makeMap("error","Not Authorized"), HttpStatus.UNAUTHORIZED);
        }

        //validate if the current user is the same as the gamePlayer given
        if(gamePlayer.getPlayer().getId() != playerService.findByUserName(authentication.getName()).getId()){
            return new ResponseEntity<>(Util.makeMap("error","Not Authorized"), HttpStatus.UNAUTHORIZED);
        }

        //calculate turn based on salvoes
        long turn = gamePlayer.getSalvos().size();

        // obtain opponent
        GamePlayer opponent = gamePlayer.getGame().getGamePlayers().stream()
                .filter(gp -> gp.getId() != gamePlayer.getId())
                .findFirst()
                .orElse(null);
        if(opponent == null){
            return new ResponseEntity<>(Util.makeMap("error","Wait for an opponent"), HttpStatus.FORBIDDEN);
        }

        //validate if salvo can be saved (my number salvos is less or equal to my opponent's)
        if(gamePlayer.getSalvos().size() > opponent.getSalvos().size())
        {
            return new ResponseEntity<>(Util.makeMap("error","Wait your turn to shoot your salvoes"), HttpStatus.FORBIDDEN);
        }

        //validate number of shoots for salvo between 1 and 5
        if(salvo.getSalvoLocations().size() >5 || salvo.getSalvoLocations().size() <1){
            return new ResponseEntity<>(Util.makeMap("error","Incorrect number of shots"), HttpStatus.FORBIDDEN);
        }

        // save salvo and add it to the gamePlayer
        salvo.setTurn(turn+1);
        gamePlayer.addSalvo(salvo);
        salvoService.saveSalvo(salvo);

        System.out.println(gamePlayer.getPlayer().getName() +" my turn : " + Long.toString(gamePlayer.getSalvos().size()));
        System.out.println(opponent.getPlayer().getName() + " enemy turn : " + Long.toString(opponent.getSalvos().size()));

        return new ResponseEntity<>((Util.makeMap("OK","Salvo Fired")),HttpStatus.CREATED);
    }

    @RequestMapping("/games/players/{nn}/salvos")
    public ResponseEntity<Map<String, Object>> getSalvoes(@PathVariable Long nn){

        Map<String,Object> dto = new LinkedHashMap<>();
        GamePlayer gp = gamePlayerService.findById(nn);

        return  new ResponseEntity<Map<String,Object>>((Util.makeMap("salvos", gp.getSalvos().stream().map(DTO::salvosToDTO).collect(Collectors.toList())))
                                                            ,HttpStatus.CREATED);
    }

}
