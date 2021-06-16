package com.codeoftheweb.salvo.service.implementation;

import com.codeoftheweb.salvo.model.Salvo;
import com.codeoftheweb.salvo.model.Ship;
import com.codeoftheweb.salvo.repository.SalvoRepository;
import com.codeoftheweb.salvo.repository.ShipRepository;
import com.codeoftheweb.salvo.service.SalvoService;
import com.codeoftheweb.salvo.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalvoServiceImpl implements SalvoService {

    @Autowired
    SalvoRepository salvoRepository;

    @Override
    public Salvo saveSalvo(Salvo salvo) {
        return salvoRepository.save(salvo);
    }

    @Override
    public List<Salvo> getSalvo() {
        return null;
    }

    @Override
    public Salvo updateSalvo(Salvo salvo) {
        return null;
    }

    @Override
    public boolean existSalvo(Long id) {
        return false;
    }

    @Override
    public Salvo findSalvoById(Long id) {
        return null;
    }

    @Override
    public List<Salvo> findAll() {
        return null;
    }
}
