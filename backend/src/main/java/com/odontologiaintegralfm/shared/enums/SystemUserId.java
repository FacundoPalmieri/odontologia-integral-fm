package com.odontologiaintegralfm.shared.enums;

/**
 * Enum que refiere al ID del usuario del sistema
 */
public enum SystemUserId {
    SYSTEM(3L);

    private final Long id;

    SystemUserId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
