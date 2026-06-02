package com.grankain.platformapi.gamer.dto.request;

import com.grankain.platformapi.infra.validation.ValidPassword;
import com.grankain.platformapi.infra.validation.ValidUser;

import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotNull(message = "Login is required.")
        @ValidUser
        String login,

        @NotNull(message = "Password is required.")
        @ValidPassword
        String password) {

}
