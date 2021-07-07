package com.ademirsoy.orderservice.service;

import com.ademirsoy.orderservice.model.Photographer;
import com.ademirsoy.orderservice.repository.PhotographerRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhotographerService {

    private final PhotographerRepository repository;

    public PhotographerService(PhotographerRepository repository) {
        this.repository = repository;
    }

    public Photographer create(Photographer photographer) {
        photographer.setCreatedDate(Instant.now());
        return repository.create(photographer);
    }

    public List<Photographer> createAll(List<Photographer> photographers) {
        List<Photographer> createdPhotographers = new ArrayList<>();
        photographers.forEach(photographer -> {
            createdPhotographers.add(this.create(photographer));
        });
        return createdPhotographers;
    }

    public Photographer retrieve(Long id) {
        Photographer photographer = repository.findById(id);
        if (photographer == null) {
            throw new EntityNotFoundException("Photographer with the following ID not found: " + id);
        }
        return photographer;
    }
}
