package com.odontologiaintegralfm.dto;

/**
 * Permite devolver una entidad simple para listar en grilla
 */
public record PermissionSimpleResponseDTO(
        Long id,
        String name,
        String label
) {
}
