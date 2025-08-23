package com.odontologiaintegralfm.infrastructure.logging.dto;

import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;

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
