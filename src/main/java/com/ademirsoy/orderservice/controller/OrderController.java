package com.ademirsoy.orderservice.controller;

import com.ademirsoy.orderservice.model.Order;
import com.ademirsoy.orderservice.model.request.AssignOrderRequest;
import com.ademirsoy.orderservice.model.request.ScheduleOrderRequest;
import com.ademirsoy.orderservice.service.OrderService;
import com.ademirsoy.orderservice.service.OrderValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/orders",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    private final OrderService orderService;
    private final OrderValidator orderValidator;

    public OrderController(OrderService orderService, OrderValidator orderValidator) {
        this.orderService = orderService;
        this.orderValidator = orderValidator;
    }

    @GetMapping
    public Collection<Order> getAll() {
        return this.orderService.retrieveAll();
    }

    @PostMapping
    public Order create(@RequestBody Order order) {
        log.info("Create order request received: " + order.toString());
        this.orderValidator.validateCreate(order);
        return this.orderService.create(order);
    }

    @PatchMapping("/{id}/schedule")
    public Order schedule(@PathVariable("id") Long orderId, @RequestBody ScheduleOrderRequest request) {
        log.info("Schedule order request received for order: " + orderId + ", request:" + request.toString());
        this.orderValidator.validateSchedule(request);
        return this.orderService.schedule(request, orderId);
    }

    @PatchMapping("/{id}/assign")
    public Order assign(@PathVariable("id") Long orderId, @RequestBody AssignOrderRequest request) {
        log.info("Assign order request received for order: " + orderId + ", photographer:" + request.getPhotographerId());
        this.orderValidator.validateAssign(request);
        return this.orderService.assign(request, orderId);
    }

    @PatchMapping("/{id}/complete")
    public Order complete(@PathVariable("id") Long orderId) {
        log.info("Complete order request received for order: " + orderId);
        return this.orderService.complete(orderId);
    }

    @PatchMapping("/{id}/reject-photos")
    public Order reject(@PathVariable("id") Long orderId) {
        log.info("Reject photos request received for order: " + orderId);
        return this.orderService.reject(orderId);
    }

    @PatchMapping("/{id}/cancel")
    public Order cancel(@PathVariable("id") Long orderId) {
        log.info("Cancel order request received for order: " + orderId);
        return this.orderService.cancel(orderId);
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MULTIPART_FORM_DATA_VALUE,
            produces = APPLICATION_JSON_VALUE,
            value = "/{id}/upload")
    public Order uploadPhoto(@PathVariable("id") Long orderId, @RequestPart(value = "file") MultipartFile file) {
        log.info("Upload photo request received for order: " + orderId);
        return this.orderService.upload(orderId, file);
    }
}
