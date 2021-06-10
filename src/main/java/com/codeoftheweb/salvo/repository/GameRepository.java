package com.codeoftheweb.salvo.repository;

import com.codeoftheweb.salvo.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByGameDate(LocalDateTime date);

    Optional<Game> findById(Long id);

    List<Game> findAll();


}
