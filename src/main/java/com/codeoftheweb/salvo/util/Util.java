package com.codeoftheweb.salvo.util;

import com.codeoftheweb.salvo.model.Player;
import com.codeoftheweb.salvo.repository.PlayerRepository;
import com.codeoftheweb.salvo.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

public class Util {


    public static Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }



    public static boolean isGuest(Authentication authentication){
        //receive an authentication object if is guest or a logged user
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
}
