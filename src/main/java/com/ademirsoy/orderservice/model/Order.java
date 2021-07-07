package com.ademirsoy.orderservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.*;
import java.util.List;
import java.util.TimeZone;

@Data
public class Order extends AbstractEntity {

    private Long id;

    private Contact contact;
    private PhotoType photoType;

    private String title;
    private String logisticInfo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime time;
    private TimeZone timezone = TimeZone.getTimeZone(ZoneId.systemDefault());
    private int durationInHours = 1;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private OrderState state;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long photographerId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> files;

    public Instant getDateTime() {
        if (this.date == null || this.time == null) {
            return null;
        }
        ZoneId zoneId = ZoneId.of(this.timezone.getID());
        return LocalDateTime.of(this.date, this.time).atZone(zoneId).toInstant();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", contact=" + contact +
                ", photoType=" + photoType +
                ", title='" + title + '\'' +
                ", logisticInfo='" + logisticInfo + '\'' +
                ", date=" + date +
                ", time=" + time +
                ", timezone=" + timezone.getID() +
                ", durationInHours=" + durationInHours +
                ", state=" + state +
                ", photographerId=" + photographerId +
                '}';
    }

}
