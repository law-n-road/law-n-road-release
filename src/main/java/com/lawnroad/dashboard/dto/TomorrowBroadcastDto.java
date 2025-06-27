package com.lawnroad.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TomorrowBroadcastDto {
    private Long scheduleNo;
    private String name;
    private LocalDate date;
    private LocalDateTime startTime;
}
