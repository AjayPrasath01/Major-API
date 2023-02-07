package com.srm.machinemonitor.Models.Requests;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddDeviceRequest {

    String machineName;

    String ssid;

    String password;

    String sensorType;

    int organization_id;

}
