package com.ademirsoy.orderservice.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.TimeZone;

@Data
public class ScheduleOrderRequest {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime time;

    //NOTE: example timezone formats: Europe/Berlin, UTC, GMT+2
    private TimeZone timezone = TimeZone.getTimeZone(ZoneId.systemDefault());

}
