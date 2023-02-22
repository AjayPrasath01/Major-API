package com.srm.machinemonitor.Controllers;

import com.srm.machinemonitor.Models.Other.CustomUserDetails;
import com.srm.machinemonitor.Models.Tables.Data;
import com.srm.machinemonitor.Models.Tables.Machines;
import com.srm.machinemonitor.Services.DataDAO;
import com.srm.machinemonitor.Services.MachinesDAO;
import com.srm.machinemonitor.Services.OrganizationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<HttpStatus> dataHandler(@RequestParam(value = "machineName", required = true)String machineName,
                                                  @RequestParam(value = "dataType", required = true)String dataType,
                                                  @RequestParam(value = "dataValue", required = true) String dataValue,
                                                  @RequestParam(value = "sensorName", required = true) String sensorName,
                                                  @RequestParam(value = "organization", required = true) String organization,
                                                  @RequestParam(value = "token", required = true) String token, Principal principal){
        /*
        chartType can be Bar, Line, No Chart
         */
        LocalDateTime date = LocalDateTime.now();
        Map res = new HashMap();
        dataType = dataType.toLowerCase();
        sensorName = sensorName.toLowerCase();
        Integer organizationId = organizationDAO.getIdByName(organization);
        if (organizationId != null && !organizationId.equals(((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId())){
            res.put("message", "Unauthorized");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
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
