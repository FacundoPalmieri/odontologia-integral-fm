package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordDTO (
        @NotBlank
        String token,

        @NotBlank(message = "userSecCreateDTO.password.empty")
        @Size(min = 10, message = "userSecCreateDTO.password.min")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{10,}",
                message = "userSecCreateDTO.password.pattern")
        String newPassword1,


        @NotBlank(message = "userSecCreateDTO.password.empty")
        @Size(min = 10, message = "userSecCreateDTO.password.min")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{10,}",
                message = "userSecCreateDTO.password.pattern")
        @NotBlank
        String newPassword2) {}
