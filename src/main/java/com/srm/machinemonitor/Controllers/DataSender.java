package com.srm.machinemonitor.Controllers;

import com.srm.machinemonitor.Models.Other.CustomUserDetails;
import com.srm.machinemonitor.Models.Tables.Data;
import com.srm.machinemonitor.Models.Tables.Machines;
import com.srm.machinemonitor.Utils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import com.srm.machinemonitor.Services.DataDAO;
import com.srm.machinemonitor.Services.OrganizationDAO;
import com.srm.machinemonitor.Services.MachinesDAO;

@RestController
@RequestMapping("/api/fetch")
@CrossOrigin
public class DataSender {

    @Autowired
    private DataDAO dataDAO;

    @Autowired
    private MachinesDAO machineDAO;

    @Autowired
    private OrganizationDAO organizationDAO;

    @GetMapping("/data")
    public ResponseEntity<List<Data>> sendMachineData(@RequestParam(value = "machineName") String machineName,
                                                      @RequestParam(value = "startDateTime", defaultValue = "1999-01-01 00:00:00", required = false) @DateTimeFormat(pattern =  "yyyy-MM-dd HH:mm:ss") LocalDateTime startDateTime,
                                                      @RequestParam(value = "stopDateTime", required = false) @DateTimeFormat(pattern =  "yyyy-MM-dd HH:mm:ss") LocalDateTime endDateTime){
        if (endDateTime == null){
            endDateTime = LocalDateTime.now();
        }
        System.out.println("machineName fetch/data " + machineName);
        System.out.println("start fetch/data " + startDateTime);
        System.out.println("end fetch/data " + endDateTime);
        // Returns date time in utc format
        return new ResponseEntity<>(dataDAO.getDataBetweenTime(machineName, startDateTime, endDateTime), HttpStatus.OK);
    }

    @GetMapping("/sensor/data")
    public ResponseEntity<List<Data>> sendMachineDataWithSensor(@RequestParam(value = "machineName") String machineName,
                                                                @RequestParam(value = "startDateTime", defaultValue = "1999-01-01 00:00:00", required = false) @DateTimeFormat(pattern =  "yyyy-MM-dd HH:mm:ss") LocalDateTime startDateTime,
                                                                @RequestParam(value = "stopDateTime", required = false) @DateTimeFormat(pattern =  "yyyy-MM-dd HH:mm:ss") LocalDateTime endDateTime,
                                                                @RequestParam(value="sensorName", required = true)String sensor, Principal principal){
        final Map<String, String> res = new HashMap<>();

        if (endDateTime == null){
            endDateTime = LocalDateTime.now();
        }
        if (principal == null){
            res.put("message", "Sign in required");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        int organization_id = ((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId();
        Machines requiredMAchine = machineDAO.getIdByMachineNameAndSensorsAndOrganizationId(machineName, sensor, organization_id);
        if (requiredMAchine == null){
            res.put("message", "Given sensor or machine name is not found");
            return new ResponseEntity(res, HttpStatus.BAD_REQUEST);
        }
        Integer machine_id = requiredMAchine.getId();
        List<Data> datas = dataDAO.getDataBetweenTimeWithSensor(machine_id, startDateTime, endDateTime);
        System.out.println("machineName sensor/data " + machineName);
        System.out.println("start sensor/data " + startDateTime);
        System.out.println("end sensor/data " + endDateTime);
        // Returns date time in utc format
//        Pre-process data and give it as X and Y
        if (datas.size() > 0){
            Map response = new HashMap();
            List<LocalDateTime> X = new ArrayList<>();
            List<Double> Y = new ArrayList<>();

            for (Data d : datas){
                X.add(d.getDate());
                Y.add(Double.parseDouble(d.getValue()));
            }

            response.put("x",X);
            response.put("y",Y);
            response.put("xAxis", "DateTime");
            response.put("yAxis", datas.get(0).getData_type());
            return new ResponseEntity(response, HttpStatus.OK);
        }
        return new ResponseEntity(datas, HttpStatus.OK);
    }

    @GetMapping("/machineNames")
    public ResponseEntity<List<String>> sendMachinesAvailable(Principal principal, HttpServletResponse response){
        final Map res = new HashMap();
        if (principal == null){
            res.put("message", "Unauthorized");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        List<Map<String, String>> data = new ArrayList<>();
        Integer organization_id = ((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId();
        if (organization_id == null){
            res.put("message", "Organization id not found");
            return new ResponseEntity(res, HttpStatus.NOT_FOUND);
        }
        System.out.println(organization_id);
        List<Machines> machinesRequired = machineDAO.findAllByOrganizationIdOrderByMachineNameAsc(organization_id);
        System.out.println(machinesRequired);
        // To combine sensorType together under single machine name
        for (Machines m : machinesRequired){
            Map temp = new HashMap<>();
            for (Map eachMachine: data){
                if (eachMachine.get("machineName").equals(m.getMachineName())){
                    temp = eachMachine;
                    break;
                }
            }
            if (temp.containsKey("sensorType")){
                temp.put("sensorType", temp.get("sensorType") + "," + m.getSensors());
            }else{
                temp.put("machineName", m.getMachineName());
                temp.put("sensorType", m.getSensors());
                data.add(temp);
            }
        }
        System.out.println(new JSONArray(data));
        return new ResponseEntity(new JSONArray(data).toString(), HttpStatus.OK);
    }

//    @GetMapping("/data/points/available")
//    public ResponseEntity sendDataPoint(@RequestParam("machinename") String machineName, @RequestParam("organization") String organizationm, @RequestParam("sensor") String sensor){
//
//    }

    @GetMapping("/csv")
    public void sendAsCSV(@RequestParam("machinename") String machinename, @RequestParam("organization") String organization,  @RequestParam("sensor") String sensor, HttpServletResponse response, Principal principal) throws IOException {
        response.setContentType("text/csv");
        String currentdate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

        String headerkey = "Content-Disposition";
        String headervalue = "attachment; filename=" + machinename + currentdate + ".csv";

        response.setHeader(headerkey, headervalue);

        Integer organization_id = organizationDAO.getIdByName(organization);

        if (organization_id == null){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "IDOR not allowed");
            return;
        }

        if (principal == null){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        if (!organization_id.equals(((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId())){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "IDOR not allowed");
            return;
        }


        Machines machineId =  machineDAO.getIdByMachineNameAndSensorsAndOrganizationId(machinename, sensor, organization_id);
        if (machineId == null){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Machine not found");
            return;
        }
        List<Data> data = dataDAO.findAllByMachineId(machineId.getId());

        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] csvHeader = {"Date", "DataType", "Sensor_Name", "Value", "Machine_Name"};
        String[] namemapping = {"date", "data_type", "sensor_name", "value", "machine_name"};

        csvWriter.writeHeader(csvHeader);

        for (Data d : data){
            csvWriter.write(d, namemapping);
        }

        csvWriter.close();

    }
}
