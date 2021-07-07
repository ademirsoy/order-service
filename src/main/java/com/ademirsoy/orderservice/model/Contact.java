package com.ademirsoy.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Contact {

    private String name;
    private String surname;
    private String email;
    private String cellNumber;
}
