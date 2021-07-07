package com.ademirsoy.orderservice.service;

import com.ademirsoy.orderservice.exception.InvalidOrderStateException;
import com.ademirsoy.orderservice.model.Order;
import com.ademirsoy.orderservice.model.OrderState;
import com.ademirsoy.orderservice.model.Photographer;
import com.ademirsoy.orderservice.model.request.AssignOrderRequest;
import com.ademirsoy.orderservice.model.request.ScheduleOrderRequest;
import com.ademirsoy.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    OrderService orderService;

    @Mock
    OrderRepository orderRepository;
    @Mock
    PhotographerService photographerService;
    @Mock
    OrderValidator orderValidator;
    @Mock
    FileUploadService fileUploadService;

    @Captor
    ArgumentCaptor<Order> orderCaptor;

    @Test
    void create_shouldSetPendingState_whenDateAndTimeProvided() {
        //GIVEN
        Order order = new Order();
        order.setDate(LocalDate.parse("2020-06-01"));
        order.setTime(LocalTime.parse("09:30:00"));

        //WHEN
        orderService.create(order);

        //THEN
        verify(orderRepository).create(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getState()).isEqualTo(OrderState.PENDING);
    }

    @Test
    void create_shouldSetUnscheduledState_whenDateAndTimeNotProvided() {
        //GIVEN
        Order order = new Order();

        //WHEN
        orderService.create(order);

        //THEN
        verify(orderRepository).create(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getState()).isEqualTo(OrderState.UNSCHEDULED);
    }

    @Test
    void schedule_shouldSetPendingState_whenRequestIsValid() {
        //GIVEN
        Order order = new Order();
        order.setId(5L);
        order.setState(OrderState.UNSCHEDULED);

        ScheduleOrderRequest request = new ScheduleOrderRequest();
        request.setDate(LocalDate.parse("2020-06-01"));
        request.setTime(LocalTime.parse("09:30:00"));

        when(orderRepository.findById(order.getId())).thenReturn(order);

        //WHEN
        orderService.schedule(request, order.getId());

        //THEN
        verify(orderRepository).update(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getState()).isEqualTo(OrderState.PENDING);
        assertThat(orderCaptor.getValue().getDate()).isEqualTo(LocalDate.parse("2020-06-01"));
    }

    @Test
    void schedule_shouldThrowException_whenOrderStateInvalid() {
        //GIVEN
        Order order = new Order();
        order.setId(5L);
        order.setState(OrderState.CANCELLED);

        ScheduleOrderRequest request = new ScheduleOrderRequest();
        request.setDate(LocalDate.parse("2020-06-01"));
        request.setTime(LocalTime.parse("09:30:00"));

        when(orderRepository.findById(order.getId())).thenReturn(order);

        //WHEN
        InvalidOrderStateException ex = Assertions.assertThrows(InvalidOrderStateException.class,
                () -> orderService.schedule(request, order.getId()));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("Only UNSCHEDULED orders can be scheduled! Current order state: CANCELLED");
    }

    @Test
    void assign_shouldSetAssignedState_whenRequestIsValid() {
        //GIVEN
        Order order = new Order();
        order.setId(5L);
        order.setState(OrderState.PENDING);

        AssignOrderRequest request = new AssignOrderRequest();
        request.setPhotographerId(1L);
        Photographer photographer = new Photographer();
        photographer.setId(1L);

        when(orderRepository.findById(order.getId())).thenReturn(order);;
        when(photographerService.retrieve(1L)).thenReturn(photographer);

        //WHEN
        orderService.assign(request, order.getId());

        //THEN
        verify(orderRepository).update(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getState()).isEqualTo(OrderState.ASSIGNED);
        assertThat(orderCaptor.getValue().getPhotographerId()).isEqualTo(1L);
    }

    @Test
    void assign_shouldThrowException_whenOrderStateInvalid() {
        //GIVEN
        Order order = new Order();
        order.setId(5L);
        order.setState(OrderState.UNSCHEDULED);

        AssignOrderRequest request = new AssignOrderRequest();
        request.setPhotographerId(1L);

        when(orderRepository.findById(order.getId())).thenReturn(order);

        //WHEN
        InvalidOrderStateException ex = Assertions.assertThrows(InvalidOrderStateException.class,
                () -> orderService.assign(request, order.getId()));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("Only PENDING orders can be assigned! Current order state: UNSCHEDULED");
    }

    @Test
    void upload_shouldSetUploadedState_whenRequestIsValid() {
        //GIVEN
        Order order = new Order();
        order.setId(5L);
        order.setState(OrderState.ASSIGNED);

        AssignOrderRequest request = new AssignOrderRequest();
        request.setPhotographerId(1L);

        MockMultipartFile mockFile = new MockMultipartFile("filename", "file".getBytes());

        when(orderRepository.findById(order.getId())).thenReturn(order);;
        when(fileUploadService.uploadFile(mockFile)).thenReturn("filename");

        //WHEN
        orderService.upload(order.getId(), mockFile);

        //THEN
        verify(orderRepository).update(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getState()).isEqualTo(OrderState.UPLOADED);
        assertThat(orderCaptor.getValue().getFiles()).hasSize(1);
        assertThat(orderCaptor.getValue().getFiles().get(0)).isEqualTo("filename");
    }

    @Test
    void upload_shouldThrowException_whenOrderStateInvalid() {
        //GIVEN
        Order order = new Order();
        order.setId(5L);
        order.setState(OrderState.COMPLETED);
        MockMultipartFile mockFile = new MockMultipartFile("filename", "filename".getBytes());

        when(orderRepository.findById(order.getId())).thenReturn(order);

        //WHEN
        InvalidOrderStateException ex = Assertions.assertThrows(InvalidOrderStateException.class,
                () -> orderService.upload(order.getId(), mockFile));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("Upload is possible Only for ASSIGNED orders! Current order state: COMPLETED");
    }

    @Test
    void complete_shouldSetCompletedState_whenRequestIsValid() {
        //GIVEN
        Order order = new Order();
        order.setId(5L);
        order.setState(OrderState.UPLOADED);

        when(orderRepository.findById(order.getId())).thenReturn(order);;

        //WHEN
        orderService.complete(order.getId());

        //THEN
        verify(orderRepository).update(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getState()).isEqualTo(OrderState.COMPLETED);
    }

    @Test
    void complete_shouldThrowException_whenOrderStateInvalid() {
        //GIVEN
        Order order = new Order();
        order.setId(5L);
        order.setState(OrderState.PENDING);

        when(orderRepository.findById(order.getId())).thenReturn(order);

        //WHEN
        InvalidOrderStateException ex = Assertions.assertThrows(InvalidOrderStateException.class,
                () -> orderService.complete(order.getId()));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("Only UPLOADED orders can be completed! Current order state: PENDING");
    }

    @Test
    void reject_shouldSetAssignedState_whenRequestIsValid() {
        //GIVEN
        Order order = new Order();
        order.setId(5L);
        order.setState(OrderState.UPLOADED);

        when(orderRepository.findById(order.getId())).thenReturn(order);;

        //WHEN
        orderService.reject(order.getId());

        //THEN
        verify(orderRepository).update(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getState()).isEqualTo(OrderState.ASSIGNED);
    }

    @Test
    void reject_shouldThrowException_whenOrderStateInvalid() {
        //GIVEN
        Order order = new Order();
        order.setId(5L);
        order.setState(OrderState.CANCELLED);

        when(orderRepository.findById(order.getId())).thenReturn(order);

        //WHEN
        InvalidOrderStateException ex = Assertions.assertThrows(InvalidOrderStateException.class,
                () -> orderService.reject(order.getId()));

        //THEN
        assertThat(ex.getMessage()).isEqualTo("Only UPLOADED orders can be rejected! Current order state: CANCELLED");
    }
}
