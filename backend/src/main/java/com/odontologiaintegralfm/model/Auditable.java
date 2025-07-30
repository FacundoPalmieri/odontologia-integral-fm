package com.odontologiaintegralfm.model;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class Auditable {

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by_id")
    private UserSec createdBy;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    private UserSec updatedBy;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;

    @ManyToOne
    @JoinColumn(name = "disabled_by_id")
    private UserSec disabledBy;
}
