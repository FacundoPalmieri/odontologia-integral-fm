package com.odontologiaintegralfm.shared.model;

import com.odontologiaintegralfm.feature.user.model.UserSec;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableJPA {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by_id")
    private UserSec createdBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    private UserSec updatedBy;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;

    @ManyToOne
    @JoinColumn(name = "disabled_by_id")
    private UserSec disabledBy;


    @PrePersist
    public void prePersist() {
        if (this.enabled == null) {
            this.enabled = true;
        }
    }

    public void disable(UserSec user) {
        if (Boolean.FALSE.equals(this.enabled)) return;
        this.enabled = false;
        this.disabledAt = LocalDateTime.now();
        this.disabledBy = user;
    }

    public void enable() {
        if (Boolean.TRUE.equals(this.enabled)) return;
        this.enabled = true;
        this.disabledAt = null;
        this.disabledBy = null;
    }
}