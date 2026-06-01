package com.grankain.platformapi.user.dto.request;

import com.grankain.platformapi.infra.validation.ValidPassword;

public record ChangePasswordRequest(
        @ValidPassword String oldPassword,
        @ValidPassword String newPassword) {

}
