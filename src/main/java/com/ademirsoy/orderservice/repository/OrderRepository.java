package com.ademirsoy.orderservice.repository;

import com.ademirsoy.orderservice.exception.DataAccessException;
import com.ademirsoy.orderservice.model.Order;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderRepository {

    private final Map<Long, Order> orderMap = new HashMap<>();

    public Order create(Order order) {
        Long id = orderMap.size() + 1L;
        order.setId(id);
        order.setCreatedDate(Instant.now());
        orderMap.put(id, order);
        return order;
    }

    public Order update(Order order) {
        if (order.getId() == null) {
            throw new DataAccessException("Order should have an ID");
        }
        order.setModifiedDate(Instant.now());
        orderMap.put(order.getId(), order);
        return order;
    }

    public Order findById(Long id) {
        return orderMap.get(id);
    }

    public Collection<Order> findAll() {
        return orderMap.values();
    }

    public List<Order> findByPhotographer(Long photographerId) {
        return this.orderMap
                .values()
                .stream()
                .filter(order -> photographerId.equals(order.getPhotographerId()))
                .collect(Collectors.toList());
    }
}
