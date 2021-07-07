package com.ademirsoy.orderservice.service;

import com.ademirsoy.orderservice.exception.EntityNotFoundException;
import com.ademirsoy.orderservice.exception.InvalidOrderStateException;
import com.ademirsoy.orderservice.model.Order;
import com.ademirsoy.orderservice.model.OrderState;
import com.ademirsoy.orderservice.model.Photographer;
import com.ademirsoy.orderservice.model.request.AssignOrderRequest;
import com.ademirsoy.orderservice.model.request.ScheduleOrderRequest;
import com.ademirsoy.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PhotographerService photographerService;
    private final OrderValidator orderValidator;
    private final FileUploadService fileUploadService;

    public OrderService(OrderRepository orderRepository, PhotographerService photographerService,
                        OrderValidator orderValidator, FileUploadService fileUploadService) {
        this.orderRepository = orderRepository;
        this.photographerService = photographerService;
        this.orderValidator = orderValidator;
        this.fileUploadService = fileUploadService;
    }

    public Order create(Order order) {
        if (order.getDateTime() == null) {
            order.setState(OrderState.UNSCHEDULED);
        } else {
            orderValidator.validateTime(order.getTime());
            order.setState(OrderState.PENDING);
        }
        return this.orderRepository.create(order);
    }

    public Collection<Order> retrieveAll() {
        return this.orderRepository.findAll();
    }

    public Order retrieve(Long id) {
        Order order = this.orderRepository.findById(id);
        if (order == null) {
            throw new EntityNotFoundException("Order with the following ID not found: " + id);
        }
        return order;
    }

    public Order schedule(ScheduleOrderRequest request, Long orderId) {
        Order order = this.retrieve(orderId);
        if (!OrderState.UNSCHEDULED.equals(order.getState())) {
            throw new InvalidOrderStateException("Only UNSCHEDULED orders can be scheduled! Current order state: " + order.getState());
        }
        orderValidator.validateTime(request.getTime());
        order.setState(OrderState.PENDING);
        order.setDate(request.getDate());
        order.setTime(request.getTime());
        order.setTimezone(request.getTimezone());
        return this.orderRepository.update(order);
    }

    public Order assign(AssignOrderRequest request, Long orderId) {
        Order order = this.retrieve(orderId);
        if (!OrderState.PENDING.equals(order.getState())) {
            throw new InvalidOrderStateException("Only PENDING orders can be assigned! Current order state: " + order.getState());
        }
        Photographer photographer = this.photographerService.retrieve(request.getPhotographerId());
        this.orderValidator.checkConflictingOrders(photographer.getId(), order);
        order.setPhotographerId(request.getPhotographerId());
        order.setState(OrderState.ASSIGNED);
        return this.orderRepository.update(order);
    }

    public Order upload(Long orderId, MultipartFile file) {
        Order order = this.retrieve(orderId);
        if (!OrderState.ASSIGNED.equals(order.getState())) {
            throw new InvalidOrderStateException("Upload is possible Only for ASSIGNED orders! Current order state: " + order.getState());
        }
        String fileName = fileUploadService.uploadFile(file);
        if (order.getFiles() == null) {
            order.setFiles(new ArrayList<>());
        }
        order.getFiles().add(fileName);
        order.setState(OrderState.UPLOADED);
        return this.orderRepository.update(order);
    }

    public Order complete(Long orderId) {
        Order order = this.retrieve(orderId);
        if (!OrderState.UPLOADED.equals(order.getState())) {
            throw new InvalidOrderStateException("Only UPLOADED orders can be completed! Current order state: " + order.getState());
        }
        order.setState(OrderState.COMPLETED);
        return this.orderRepository.update(order);
    }

    public Order reject(Long orderId) {
        Order order = this.retrieve(orderId);
        if (!OrderState.UPLOADED.equals(order.getState())) {
            throw new InvalidOrderStateException("Only UPLOADED orders can be rejected! Current order state: " + order.getState());
        }
        order.setState(OrderState.ASSIGNED);
        return this.orderRepository.update(order);
    }

    public Order cancel(Long orderId) {
        Order order = this.retrieve(orderId);
        order.setState(OrderState.CANCELLED);
        return this.orderRepository.update(order);
    }
}
