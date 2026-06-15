package com.redgit.auth.controllers.DTO;

import com.redgit.auth.infrastructure.entity.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRoleDTO {

    @NotNull(message = "Role é obrigatória")
    private UserRole role;
}