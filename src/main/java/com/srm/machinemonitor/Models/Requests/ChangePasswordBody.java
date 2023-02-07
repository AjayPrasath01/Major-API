package com.srm.machinemonitor.Models.Requests;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class ChangePasswordBody {
    String username;
    String currentPassword;
    String newPassword;
}
