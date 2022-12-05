package com.codeoftheweb.salvo.DTO;

import com.codeoftheweb.salvo.model.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class DTO {


    public static Map<String, Object> gameToDTO(Game game) {
        /** Return game information
         *
         */
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("created", game.getGameDate());
        dto.put("gamePlayers", game.getGamePlayers().stream()
                .map(gamePlayer -> DTO.gamePlayersToDTO(gamePlayer))
                .collect(toList()));

        dto.put("scores",   game.getGamePlayers().stream()
                .map(gp -> DTO.scoresToDTO(gp))
                .collect(toList()));
        return dto;
    }


    public static Map<String,Object> scoresToDTO(GamePlayer gp) {
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

    public static Map<String,Object> shipsToDTO(Ship sh) {
        // Return Ship information
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type",sh.getType());
        dto.put("locations",sh.getShipLocations());

        return dto;
    }

    public static Map<String, Object> gamePlayersToDTO(GamePlayer gp) {
        //return GamePlayer Information
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id",gp.getId());
        dto.put("player", playersToDTO(gp.getPlayer()));
        return dto;
    }

    public static Map<String, Object> playersToDTO(Player p){
        //return Player Information
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id",p.getId());
        dto.put("email",p.getUserName());

        return dto;
    }

    public static Map<String, Object> salvosToDTO(Salvo s){
        //return Salvo Information
        Map<String,Object> dto = new LinkedHashMap<>();
        //flat list
        dto.put("turn",s.getTurn());
        dto.put("player",s.getGamePlayer().getPlayer().getId());
        dto.put("locations",s.getSalvoLocations());

        return dto;
    }
}
