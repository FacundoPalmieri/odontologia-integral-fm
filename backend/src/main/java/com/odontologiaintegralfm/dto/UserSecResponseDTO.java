package com.odontologiaintegralfm.dto;

import com.odontologiaintegralfm.model.Role;
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
    private PersonResponseDTO personResponseDTO;   //Optional
    private DentistResponseDTO dentistResponseDTO; //Optional
}
