package com.srm.machinemonitor.Models.Requests;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChangePasswordBody {
    @NotNull(message="Username can't be null")
    String username;
    @NotNull(message="currentPassword cant be null")
    String currentPassword;
    @NotNull(message="newPassword cant be null")
    String newPassword;
    @NotNull(message="organization cant be null")
    String organization;
}
