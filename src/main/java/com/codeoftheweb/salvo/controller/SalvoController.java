package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.model.*;
import com.codeoftheweb.salvo.repository.GamePlayerRepository;
import com.codeoftheweb.salvo.service.GamePlayerService;
import com.codeoftheweb.salvo.service.GameService;

import com.codeoftheweb.salvo.service.PlayerService;
import com.codeoftheweb.salvo.util.util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
                                .map(game -> this.gameToDTO(game))
                                .collect(toList()));
        if(!isGuest(authentication)) {
            Map<String,Object> dtoPlayer = new LinkedHashMap<>();
            dtoPlayer.put("id",getUser(authentication).getId());
            dtoPlayer.put("email",getUser(authentication).getName());
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

        if(isGuest(authentication)) {
            return new ResponseEntity<Map<String, Object>>(util.makeMap("error","Not Authorized"),HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> gpMap = new LinkedHashMap<String,Object>();
        GamePlayer gamePlayer = gamePlayerService.findById(nn);

        if(gamePlayer == null) {
            return new ResponseEntity<Map<String, Object>>(util.makeMap("error","Not Authorized"),HttpStatus.UNAUTHORIZED);
        }

        if(gamePlayer.getPlayer().getId() != playerService.findByUserName(authentication.getName()).getId())
        {
            return new ResponseEntity<Map<String, Object>>(util.makeMap("error","Not Authorized"),HttpStatus.UNAUTHORIZED);
        }

        Set<Ship> ships = gamePlayer.getShips();
        Game game = gamePlayer.getGame();
        Set<GamePlayer> players = game.getGamePlayers();
        Set<Salvo> salvos = players.stream().map(GamePlayer::getSalvoes).flatMap(Collection::stream).collect(Collectors.toSet());

        Map<String,Object> hits = new LinkedHashMap<String,Object>();
        hits.put("self", new ArrayList<>());
        hits.put("opponent", new ArrayList<>());


        //Game Information
        gpMap = gameToDTO(gamePlayer.getGame());
        //Game State
        gpMap.put("gameState", "PLACESHIPS");
        //Ships Information
        gpMap.put("ships", ships.stream()
                .map(sh -> this.shipsToDTO(sh))
                .collect(toList()));

        //Salvos Information
        gpMap.put("salvoes", salvos.stream()
                .map(sal -> this.salvosToDTO(sal))
                .collect(toList()));

        gpMap.put("hits", hits);

        return new ResponseEntity<Map<String, Object>>(gpMap, HttpStatus.OK);
    }



    private Map<String, Object> gameToDTO(Game game) {
        /** Return game information
         *
         */
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("created", game.getGameDate());
        dto.put("gamePlayers", game.getGamePlayers().stream()
                .map(gamePlayer -> this.gamePlayersToDTO(gamePlayer))
                .collect(toList()));

        dto.put("scores",   game.getGamePlayers().stream()
                                        .map(gp -> this.scoresToDTO(gp))
                                        .collect(toList()));
        return dto;
    }


    private Map<String,Object> scoresToDTO(GamePlayer gp) {
        //return Score information
        Map<String,Object> dto = new LinkedHashMap<>();
        //dto.put("id",gp.getScore(gp.getGame()).getId());

        if(gp.getScore(gp.getGame()) != null) {
            dto.put("score", gp.getScore(gp.getGame()).getPoints());
        }else{
            dto.put("score", null);
        }
        dto.put("player", gp.getPlayer().getId());
        //dto.put("finish_date",gp.getScore(gp.getGame()).getFinishDate());
        return dto;
    }

    private Map<String,Object> shipsToDTO(Ship sh) {
        // Return Ship information
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type",sh.getType());
        dto.put("locations",sh.getLocations());

        return dto;
    }

    private Map<String, Object> gamePlayersToDTO(GamePlayer gp) {
        //return GamePlayer Information
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id",gp.getId());
        dto.put("player", playersToDTO(gp.getPlayer()));
        return dto;
    }

    private Map<String, Object> playersToDTO(Player p){
        //return Player Information
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id",p.getId());
        dto.put("email",p.getUserName());

        return dto;
    }

    private Map<String, Object> salvosToDTO(Salvo s){
        //return Salvo Information
        Map<String,Object> dto = new LinkedHashMap<>();
        //flat list
        dto.put("turn",s.getTurn());
        dto.put("player",s.getGamePlayer().getPlayer().getId());
        dto.put("locations",s.getLocations());

        return dto;
    }



    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String email, @RequestParam String password) {

        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(util.makeMap("error","Missing data"), HttpStatus.FORBIDDEN);
        }

        if (playerService.findByUserName(email) !=  null) {
            return new ResponseEntity<>(util.makeMap("error","Name already in use"), HttpStatus.FORBIDDEN);
        }

        playerService.savePlayer(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Object> addGame(Authentication authentication){
        if(isGuest(authentication)){
            return new ResponseEntity<>(util.makeMap("error","Not Authorized"), HttpStatus.UNAUTHORIZED);
        }else {
            Game game = gameService.saveGame(new Game(LocalDateTime.now()));
            GamePlayer gamePlayer = gamePlayerService.saveGamePlayer(new GamePlayer(game, playerService.findByUserName(authentication.getName()),LocalDateTime.now()));
            return new ResponseEntity<>((util.makeMap("gpid",gamePlayer.getId())),HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/game/{nn}/players", method = RequestMethod.POST)
    public ResponseEntity<Object> joinGame(@PathVariable Long nn, Authentication authentication){
        if(isGuest(authentication)){
            return new ResponseEntity<>(util.makeMap("error","Not Authorized"), HttpStatus.UNAUTHORIZED);
        }else {
            Game game = gameService.findById(nn);
            long n_players = game.getGamePlayers().size();
            if (game == null) {
                return new ResponseEntity<>(util.makeMap("error","No Such game"), HttpStatus.FORBIDDEN);
            }
            if( n_players>1){
                return new ResponseEntity<>(util.makeMap("error","Game is full"), HttpStatus.FORBIDDEN);
            }
            GamePlayer gamePlayer = gamePlayerService.saveGamePlayer(new GamePlayer(game,
                                                                        playerService.findByUserName(authentication.getName()),
                                                                        LocalDateTime.now()));
            return new ResponseEntity<>((util.makeMap("gpid",gamePlayer.getId())),HttpStatus.CREATED);
        }
    }


    private Player getUser(Authentication authentication) {
        if(authentication != null)
            return playerService.findByUserName(authentication.getName());
        else
            return null;
    }

    private boolean isGuest(Authentication authentication){
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

}
