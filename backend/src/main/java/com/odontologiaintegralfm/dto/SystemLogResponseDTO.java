package com.odontologiaintegralfm.dto;

import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.enums.LogType;

import java.util.Map;


public record SystemLogResponseDTO(
        LogLevel level,
        LogType type,
        String userMessage,
        String technicalMessage,
        String name,
        String username,
        Map<String, Object> metadata,
        String stackTrace
) {
}
