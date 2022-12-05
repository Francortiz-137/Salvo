package com.codeoftheweb.salvo.service;

import com.codeoftheweb.salvo.model.Salvo;

import java.util.List;

public interface SalvoService {

    Salvo saveSalvo(Salvo salvo);

    List<Salvo> getSalvo();

    Salvo updateSalvo(Salvo salvo);

    boolean existSalvo(Long id);

    Salvo findSalvoById(Long id);

    List<Salvo> findAll();

}
