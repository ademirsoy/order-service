package com.ademirsoy.orderservice.service;

import com.ademirsoy.orderservice.exception.BadRequestException;
import com.ademirsoy.orderservice.exception.ConflictingOrderException;
import com.ademirsoy.orderservice.model.Contact;
import com.ademirsoy.orderservice.model.Order;
import com.ademirsoy.orderservice.model.OrderState;
import com.ademirsoy.orderservice.model.request.AssignOrderRequest;
import com.ademirsoy.orderservice.model.request.ScheduleOrderRequest;
import com.ademirsoy.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderValidator {

    public static final Pattern EMAIL_PATTERN = Pattern.compile("^(.+)@(.+)$");
    private final OrderRepository orderRepository;

    public OrderValidator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void validateCreate(Order order) {
        Contact contact = order.getContact();
        if (contact == null) {
            throw new BadRequestException("contact field is mandatory!");
        }
        boolean missingContactData = Stream
                .of(contact.getCellNumber(), contact.getEmail(), contact.getName(), contact.getSurname())
                .anyMatch(field -> field == null || "".equals(field));

        if (missingContactData) {
            throw new BadRequestException("All fields in contact data are mandatory: name, surname, email, cellNumber");
        }

        if (!EMAIL_PATTERN.matcher(contact.getEmail()).matches()) {
            throw new BadRequestException("Invalid email!");
        }

        if (order.getPhotoType() == null) {
            throw new BadRequestException("photoType field is mandatory!");
        }

        if (order.getPhotographerId() != null) {
            throw new BadRequestException("photographerId cannot be assigned at the time of creation!");
        }
    }

    public void validateSchedule(ScheduleOrderRequest request) {
        if (request.getDate() == null) {
            throw new BadRequestException("date field is mandatory!");
        }
        if (request.getTime() == null) {
            throw new BadRequestException("time field is mandatory!");
        }
    }

    public void validateAssign(AssignOrderRequest request) {
        if (request.getPhotographerId() == null) {
            throw new BadRequestException("photographerId field is mandatory!");
        }
    }

    public void checkConflictingOrders(Long photographerId, Order order) {
        List<Order> existingOrders = this.orderRepository.findByPhotographer(photographerId);
        List<Order> conflictingOrders = existingOrders
                .stream()
                .filter(o -> OrderState.ASSIGNED.equals(o.getState()))
                .filter(o -> {
                    if (o.getDateTime().isBefore(order.getDateTime())
                            && o.getDateTime().plus(order.getDurationInHours(), ChronoUnit.HOURS).isAfter(order.getDateTime())) {
                        return true;
                    } else if (order.getDateTime().isBefore(o.getDateTime())
                            && order.getDateTime().plus(order.getDurationInHours(), ChronoUnit.HOURS).isAfter(o.getDateTime())) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        if (conflictingOrders.size() > 0) {
            throw new ConflictingOrderException("This photographer is not available at the requested date and time!");
        }
    }

    public void validateTime(LocalTime time) {
        if (time.isBefore(LocalTime.parse("08:00:00")) || time.isAfter(LocalTime.parse("20:00:00"))) {
            throw new BadRequestException("Order must be within business hours: (8:00-20:00)");
        }
    }
}
