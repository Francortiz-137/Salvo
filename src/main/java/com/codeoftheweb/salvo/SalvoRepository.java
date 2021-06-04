package com.codeoftheweb.salvo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalvoRepository extends JpaRepository<Salvo, Long> {
    List<Player> findByTurn(int turn);
}
