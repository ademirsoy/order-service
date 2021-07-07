package com.ademirsoy.orderservice.model;

import lombok.Data;

@Data
public class Photographer extends AbstractEntity {

    private Long id;
    private String name;
}
