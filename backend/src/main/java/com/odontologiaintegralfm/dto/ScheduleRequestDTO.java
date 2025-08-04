package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @author [Facundo Palmieri]
 */
public record ScheduleRequestDTO(
        @NotNull(message = "generic.id.empty")
        Long id,

        @NotBlank(message = "scheduleConfigRequestDTO.cronExpression.empty")
        String cronExpression
){}
