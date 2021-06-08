package com.codeoftheweb.salvo.repository;

import com.codeoftheweb.salvo.model.Player;
import com.codeoftheweb.salvo.model.Salvo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalvoRepository extends JpaRepository<Salvo, Long> {
    List<Player> findByTurn(int turn);
}
