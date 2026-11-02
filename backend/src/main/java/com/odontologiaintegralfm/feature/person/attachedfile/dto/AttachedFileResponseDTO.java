package com.odontologiaintegralfm.feature.person.attachedfile.dto;

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
