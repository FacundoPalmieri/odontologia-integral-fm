package com.odontologiaintegralfm.feature.user.dto;

import com.odontologiaintegralfm.feature.dentist.core.dto.DentistResponseDTO;
import com.odontologiaintegralfm.feature.person.core.dto.PersonResponseDTO;
import com.odontologiaintegralfm.feature.authentication.model.Role;
import lombok.*;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSecResponseDTO {

    private Long id;
    private String username;
    private Set<Role> rolesList;
    private boolean enabled;

    private PersonResponseDTO person;   //Optional
    private DentistResponseDTO dentist; //Optional
}
