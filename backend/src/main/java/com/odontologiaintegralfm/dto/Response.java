package com.odontologiaintegralfm.dto;

public record Response<T>(

        boolean success,
        String message,
        T data // Puedes generalizar el tipo de datos a devolver
) {
}
