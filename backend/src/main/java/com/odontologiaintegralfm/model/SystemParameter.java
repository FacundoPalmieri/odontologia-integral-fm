package com.odontologiaintegralfm.model;

import com.odontologiaintegralfm.enums.SystemParameterKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "system_parameters")
public class SystemParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SystemParameterKey keyName;

    @Column(nullable = false, length = 20)
    private String value;

    @Column(nullable = false)
    private String description;
}
