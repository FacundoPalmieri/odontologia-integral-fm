package com.odontologiaintegralfm.feature.user.dto;

import com.odontologiaintegralfm.feature.dentist.core.dto.DentistCreateRequestDTO;
import com.odontologiaintegralfm.feature.person.core.dto.PersonCreateRequestDTO;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class UserSecCreateDTO {
    @NotBlank(message = "userSecCreateDTO.username.empty")
    @Email(message = "userSecCreateDTO.username.email")
    private String username;

    @NotBlank(message = "userSecCreateDTO.password.empty")
    @Size(min = 10, message = "userSecCreateDTO.password.min")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{10,}",
            message = "userSecCreateDTO.password.pattern")
    private String password1;

    @NotBlank(message = "userSecCreateDTO.password.empty")
    @Size(min = 10, message = "userSecCreateDTO.password.min")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{10,}",
            message = "userSecCreateDTO.password.pattern")
    private String password2;

    @NotNull(message = "userSecCreateDTO.role.empty")
    private Set<Long> rolesList;

    //Opcional
    private PersonCreateRequestDTO person;

    //Opcional
    private DentistCreateRequestDTO dentist;
}

