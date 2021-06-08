package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.model.*;
import com.codeoftheweb.salvo.repository.PlayerRepository;
import com.codeoftheweb.salvo.service.GamePlayerService;
import com.codeoftheweb.salvo.service.GameService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
    //private GameRepository gameRepo;
    @Autowired
    private GamePlayerService gamePlayerService;
    //private GamePlayerRepository gpRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PlayerRepository playerRepository;

    @RequestMapping("/games")
    public Map<String, Object> getGames(Authentication authentication)
    {


        Map<String, Object> gMap = new LinkedHashMap<String,Object>();
        List<Map<String, Object>> games = new ArrayList<>();


        gMap.put("games",gameService.findAll().stream()
                                .map(game -> this.gameToDTO(game))
                                .collect(toList()));
        if(getUser(authentication)!= null) {
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
    public Map<String,Object> getGameView(@PathVariable Long nn){
        //Return Map with a specific game information
        //nn: Gameplayer Id

        Map<String, Object> gpMap = new LinkedHashMap<String,Object>();

        GamePlayer gamePlayer = gamePlayerService.findGamePlayerById(nn);

        Set<Ship> ships = gamePlayer.getShips();
        List<GamePlayer> players = gamePlayer.getGame().getGamePlayers();
        Set<Salvo> salvos = players.stream().map(GamePlayer::getSalvoes).flatMap(Collection::stream).collect(Collectors.toSet());

        //Game Information
        gpMap = gameToDTO(gamePlayer.getGame());
        //Ships Information
        gpMap.put("ships", ships.stream()
                .map(sh -> this.shipsToDTO(sh))
                .collect(toList()));

        //Salvos Information
        gpMap.put("salvoes", salvos.stream()
                .map(sal -> this.salvosToDTO(sal))
                .collect(toList()));

        return gpMap;
    }


    private Map<String, Object> gameToDTO(Game game) {
        // Return game information
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
        dto.put("id", gp.getId());
        dto.put("player", playersToDTO(gp.getPlayer()));
        if(gp.getScore(gp.getGame()) != null)
            dto.put("score", gp.getScore(gp.getGame()).getPoints());
        else
            dto.put("score", null);

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

        //object with turn as keys
        /*
        dto.put(s.getTurn(),new LinkedHashMap<>(s.getGamePlayer().getPlayer().getId()), s.getLocations()));
         */

        // An object with player IDs as keys, and within each player, an object with turns as keys
        /*
            dto.put(s.getGamePlayer().getPlayer().getId(), new LinkedHashMap<>( s.getTurn(), s.getLocations()));
         */

        return dto;
    }




    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(
            @RequestParam String email, @RequestParam String password) {

        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(email) !=  null) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private Player getUser(Authentication authentication) {
        if(authentication != null)
            return playerRepository.findByUserName(authentication.getName());
        else
            return null;
    }

}
