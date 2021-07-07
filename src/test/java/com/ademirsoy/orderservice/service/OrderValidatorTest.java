package com.ademirsoy.orderservice.service;

import com.ademirsoy.orderservice.exception.BadRequestException;
import com.ademirsoy.orderservice.exception.ConflictingOrderException;
import com.ademirsoy.orderservice.model.Contact;
import com.ademirsoy.orderservice.model.Order;
import com.ademirsoy.orderservice.model.OrderState;
import com.ademirsoy.orderservice.model.PhotoType;
import com.ademirsoy.orderservice.model.request.AssignOrderRequest;
import com.ademirsoy.orderservice.model.request.ScheduleOrderRequest;
import com.ademirsoy.orderservice.repository.OrderRepository;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderValidatorTest {

    @InjectMocks
    OrderValidator orderValidator;

    @Mock
    OrderRepository orderRepository;

    @Test
    void validateCreate_shouldValidate_whenOrderIsValid() {
        //GIVEN
        Order order = new Order();
        order.setContact(new Contact("lorenzo", "insigne", "marco@polo.com", "1234567890"));
        order.setPhotoType(PhotoType.Food);

        //WHEN-THEN
        Assertions.assertDoesNotThrow(() -> orderValidator.validateCreate(order));
    }

    @Test
    void validateCreate_shouldThrowException_whenContactDataMissing() {
        //GIVEN
        Order order = new Order();
        order.setContact(new Contact("lorenzo", "", "marco@polo.com", "1234567890"));
        order.setPhotoType(PhotoType.Food);

        //WHEN
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () -> orderValidator.validateCreate(order));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("All fields in contact data are mandatory: name, surname, email, cellNumber");

    }

    @Test
    void validateCreate_shouldThrowException_whenEmailNotValid() {
        //GIVEN
        Order order = new Order();
        order.setContact(new Contact("lorenzo", "insigne", "@polo.com", "1234567890"));
        order.setPhotoType(PhotoType.Food);

        //WHEN
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () -> orderValidator.validateCreate(order));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("Invalid email!");

    }

    @Test
    void validateCreate_shouldThrowException_whenPhotoTypeMissing() {
        //GIVEN
        Order order = new Order();
        order.setContact(new Contact("lorenzo", "insigne", "marco@polo.com", "1234567890"));
        order.setPhotoType(null);

        //WHEN
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () -> orderValidator.validateCreate(order));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("photoType field is mandatory!");
    }

    @Test
    void validateSchedule_shouldValidate_whenRequestIsValid() {
        //GIVEN
        ScheduleOrderRequest request = new ScheduleOrderRequest();
        request.setDate(LocalDate.parse("2020-01-02"));
        request.setTime(LocalTime.parse("10:30:00"));

        //WHEN
        Assertions.assertDoesNotThrow(() -> orderValidator.validateSchedule(request));
    }

    @Test
    void validateSchedule_shouldThrowException_whenMissingDate() {
        //GIVEN
        ScheduleOrderRequest request = new ScheduleOrderRequest();
        request.setTime(LocalTime.parse("10:30:00"));

        //WHEN
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () -> orderValidator.validateSchedule(request));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("date field is mandatory!");
    }

    @Test
    void validateAssign_shouldValidate_whenRequestIsValid() {
        //GIVEN
        AssignOrderRequest request = new AssignOrderRequest();
        request.setPhotographerId(5L);

        //WHEN-THEN
        Assertions.assertDoesNotThrow(() -> orderValidator.validateAssign(request));
    }

    @Test
    void validateAssign_shouldThrowException_whenMissingDate() {
        //GIVEN
        AssignOrderRequest request = new AssignOrderRequest();

        //WHEN
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () -> orderValidator.validateAssign(request));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("photographerId field is mandatory!");
    }

    @Test
    void checkConflictingOrders_shouldThrowException_whenConflictingOrdersExist() {
        //GIVEN
        Order order = new Order();
        order.setDate(LocalDate.parse("2020-06-01"));
        order.setTime(LocalTime.parse("10:30:00"));

        Order existingOrder1 = new Order();
        existingOrder1.setState(OrderState.ASSIGNED);
        existingOrder1.setDate(LocalDate.parse("2020-06-01"));
        existingOrder1.setTime(LocalTime.parse("10:00:00"));
        Order existingOrder2 = new Order();
        existingOrder2.setState(OrderState.ASSIGNED);
        existingOrder2.setDate(LocalDate.parse("2020-06-01"));
        existingOrder2.setTime(LocalTime.parse("12:00:00"));

        List<Order> existingOrders = Arrays.asList(existingOrder1, existingOrder2);
        when(orderRepository.findByPhotographer(5L)).thenReturn(existingOrders);

        //WHEN
        ConflictingOrderException ex = Assertions.assertThrows(ConflictingOrderException.class,
                () -> orderValidator.checkConflictingOrders(5L, order));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("This photographer is not available at the requested date and time!");
    }

    @Test
    void checkConflictingOrders_shouldThrowException_whenConflictingOrdersExistBecauseOfDuration() {
        //GIVEN
        Order order = new Order();
        order.setDate(LocalDate.parse("2020-06-01"));
        order.setTime(LocalTime.parse("09:30:00"));
        order.setDurationInHours(3);

        Order existingOrder1 = new Order();
        existingOrder1.setState(OrderState.ASSIGNED);
        existingOrder1.setDate(LocalDate.parse("2020-06-01"));
        existingOrder1.setTime(LocalTime.parse("11:00:00"));
        Order existingOrder2 = new Order();
        existingOrder2.setState(OrderState.ASSIGNED);
        existingOrder2.setDate(LocalDate.parse("2020-06-01"));
        existingOrder2.setTime(LocalTime.parse("12:00:00"));

        List<Order> existingOrders = Arrays.asList(existingOrder1, existingOrder2);
        when(orderRepository.findByPhotographer(5L)).thenReturn(existingOrders);

        //WHEN
        ConflictingOrderException ex = Assertions.assertThrows(ConflictingOrderException.class,
                () -> orderValidator.checkConflictingOrders(5L, order));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("This photographer is not available at the requested date and time!");

    }

    @Test
    void checkConflictingOrders_shouldValidate_whenNoConflict() {
        //GIVEN
        Order order = new Order();
        order.setDate(LocalDate.parse("2020-06-01"));
        order.setTime(LocalTime.parse("10:30:00"));
        order.setTimezone(TimeZone.getTimeZone("America/Los_Angeles"));

        Order existingOrder1 = new Order();
        existingOrder1.setState(OrderState.ASSIGNED);
        existingOrder1.setDate(LocalDate.parse("2020-06-01"));
        existingOrder1.setTime(LocalTime.parse("11:00:00"));
        existingOrder1.setTimezone(TimeZone.getTimeZone("Europe/Berlin"));
        Order existingOrder2 = new Order();
        existingOrder2.setState(OrderState.ASSIGNED);
        existingOrder2.setDate(LocalDate.parse("2020-06-01"));
        existingOrder2.setTime(LocalTime.parse("12:00:00"));
        existingOrder2.setTimezone(TimeZone.getTimeZone("Europe/Berlin"));

        List<Order> existingOrders = Arrays.asList(existingOrder1, existingOrder2);
        when(orderRepository.findByPhotographer(5L)).thenReturn(existingOrders);

        //WHEN
        Assertions.assertDoesNotThrow(() -> orderValidator.checkConflictingOrders(5L, order));
    }

    @Test
    void validateTime_shouldValidate_whenTimeInBusinessHours() {
        //GIVEN
        LocalTime businessTime = LocalTime.parse("08:00:00");

        //WHEN
        Assertions.assertDoesNotThrow(() -> orderValidator.validateTime(businessTime));
    }

    @Test
    void validateTime_shouldThrowException_whenTimeInOffBusinessHoursMorning() {
        //GIVEN
        LocalTime offTime = LocalTime.parse("03:30:00");

        //WHEN
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () -> orderValidator.validateTime(offTime));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("Order must be within business hours: (8:00-20:00)");
    }

    @Test
    void validateTime_shouldThrowException_whenTimeInOffBusinessHoursEvening() {
        //GIVEN
        LocalTime offTime = LocalTime.parse("21:30:00");

        //WHEN
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () -> orderValidator.validateTime(offTime));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("Order must be within business hours: (8:00-20:00)");
    }
}
