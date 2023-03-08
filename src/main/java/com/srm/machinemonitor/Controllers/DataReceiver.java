package com.srm.machinemonitor.Controllers;

import com.srm.machinemonitor.Models.Other.CustomUserDetails;
import com.srm.machinemonitor.Models.Tables.Data;
import com.srm.machinemonitor.Models.Tables.Machines;
import com.srm.machinemonitor.Services.DataDAO;
import com.srm.machinemonitor.Services.OrganizationDAO;
import com.srm.machinemonitor.Services.MachinesDAO;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.header.Header;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.math.BigInteger;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/setter")
@CrossOrigin
public class DataReceiver {

    @Autowired
    DataDAO dataDAO;

    @Autowired
    MachinesDAO machinesDAO;

    @Autowired
    OrganizationDAO organizationDAO;

    @GetMapping("/data")
    public ResponseEntity<HttpStatus> dataHandler(@RequestParam(value = "machineName", required = true) @NotBlank String machineName,
                                                  @RequestParam(value = "dataType", required = true) @NotBlank String dataType,
                                                  @RequestParam(value = "dataValue", required = true) @NotBlank String dataValue,
                                                  @RequestParam(value = "sensorName", required = true) @NotBlank String sensorName,
                                                  @RequestParam(value = "organization", required = true) @NotBlank String organization,
                                                  @RequestParam(value = "token", required = true) String token, HttpServletRequest request){
        /*
        chartType can be Bar, Line, No Chart
         */
        LocalDateTime date = LocalDateTime.now();
        Map res = new HashMap();
        dataType = dataType.toLowerCase();
        sensorName = sensorName.toLowerCase();

        BigInteger organizationId = organizationDAO.getIdByName(organization);

        if (organizationId == null){
            res.put("message", "Invalid organization");
            return new ResponseEntity(res, HttpStatus.BAD_REQUEST);
        }

        Machines machine = machinesDAO.findByMachineNameAndSensorsAndOrganizationId(machineName.toLowerCase(), sensorName, organizationId);
        if (machine == null){
            res.put("message", "Machine not found");
            return new ResponseEntity(res, HttpStatus.NOT_FOUND);
        }
        if (!Objects.equals(token, machine.getSecert())){
            res.put("message", "Unauthorized");
            return new ResponseEntity(res, HttpStatus.BAD_REQUEST);
        }

        Data data = new Data();
        data.setDate(date);
        data.setData_type(dataType);
        data.setMachineId(machine.getId());
        data.setValue(dataValue);

        dataDAO.save(data);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
