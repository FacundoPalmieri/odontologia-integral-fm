package com.odontologiaintegralfm.dto;

import java.time.LocalDateTime;

/**
 * @author [Facundo Palmieri]
 */
public record AttachedFileResponseDTO(
        Long id,
        String originalFileName,
        String storedFileName,
        LocalDateTime createdAt,
        String createdBy,
        String url
) {
}
