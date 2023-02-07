package com.srm.machinemonitor.Controllers;

import com.srm.machinemonitor.Models.Tables.Data;
import com.srm.machinemonitor.Services.DataDAO;
import com.srm.machinemonitor.Services.MachinesDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/setter")
@CrossOrigin
public class DataReceiver {

    @Autowired
    DataDAO dataDAO;

    @Autowired
    MachinesDAO machinesDAO;

    @GetMapping("/data")
    public ResponseEntity<HttpStatus> dataHandler(@RequestParam(value = "machineName", required = true)String machineName,
                                      @RequestParam(value = "dataType", required = true)String dataType,
                                      @RequestParam(value = "dataValue", required = true) String dataValue,
                                      @RequestParam(value = "sensorName", required = true) String sensorName){
        /*
        chartType can be Bar, Line, No Chart
         */
        LocalDateTime date = LocalDateTime.now();
        if (!machinesDAO.existsBymachineName(machineName)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        dataType = dataType.toLowerCase();
        sensorName = sensorName.toUpperCase();
        Data data = new Data();
        data.setSensor_name(sensorName);
        data.setDate(date);
        data.setData_type(dataType);
        data.setMachine_name(machineName);
        data.setValue(dataValue);

        dataDAO.save(data);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
