package com.ademirsoy.orderservice.repository;

import com.ademirsoy.orderservice.exception.DataAccessException;
import com.ademirsoy.orderservice.model.Photographer;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class PhotographerRepository {

    private final Map<Long, Photographer> photographerMap = new HashMap<>();

    public Photographer create(Photographer photographer) {
        Long id = photographerMap.size() + 1L;
        photographer.setId(id);
        photographer.setCreatedDate(Instant.now());
        photographerMap.put(id, photographer);
        return photographer;
    }

    public Photographer update(Photographer photographer) {
        if (photographer.getId() == null) {
            throw new DataAccessException("Order should have an ID");
        }
        photographer.setModifiedDate(Instant.now());
        photographerMap.put(photographer.getId(), photographer);
        return photographer;
    }

    public Photographer findById(Long id) {
        return photographerMap.get(id);
    }
}
