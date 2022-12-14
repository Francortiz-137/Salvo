package com.codeoftheweb.salvo.repository;

import com.codeoftheweb.salvo.model.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    List<GamePlayer> findByDate(LocalDateTime date);

    Optional<GamePlayer> findById(Long id);

    List<GamePlayer> findAll();
}
