package com.srm.machinemonitor.Models.Requests;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddDeviceRequest {

    @NotNull(message="machineName can't be blank")
    String machineName;

    @NotNull(message="machineName can't be blank")
    String connectionType; // Wifi, Eth, func

    String ssid;

    String password;

    @NotNull(message="sensorType can't be blank")
    String sensors;

    @NotNull(message="organization_id can't be blank")
    String organization;

}
