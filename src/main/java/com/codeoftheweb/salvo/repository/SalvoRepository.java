package com.codeoftheweb.salvo.repository;

import com.codeoftheweb.salvo.model.Player;
import com.codeoftheweb.salvo.model.Salvo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface SalvoRepository extends JpaRepository<Salvo, Long> {
    List<Salvo> findByTurn(int turn);


}
