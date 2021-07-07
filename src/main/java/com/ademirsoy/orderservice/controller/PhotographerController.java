package com.ademirsoy.orderservice.controller;

import com.ademirsoy.orderservice.model.Photographer;
import com.ademirsoy.orderservice.service.PhotographerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/photographers",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class PhotographerController {

    private final PhotographerService photographerService;

    public PhotographerController(PhotographerService photographerService) {
        this.photographerService = photographerService;
    }

    @PostMapping
    public Photographer create(@RequestBody Photographer photographer) {
        log.info("Create photographer request received for: " + photographer.getName());
        return photographerService.create(photographer);
    }

    @PostMapping("/bulk")
    public List<Photographer> createInBulk(@RequestBody List<Photographer> photographers) {
        log.info("Create photographers request received for: " + photographers);
        return photographerService.createAll(photographers);
    }
}
