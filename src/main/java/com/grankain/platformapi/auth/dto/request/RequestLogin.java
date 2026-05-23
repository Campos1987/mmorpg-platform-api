package com.grankain.platformapi.auth.dto.request;

import com.grankain.platformapi.infra.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestLogin(
        @NotBlank(message = "Username é obrigatório")
        @Size(min = 5, max = 100)
        String user,

        @NotBlank
        String password
) {
}
