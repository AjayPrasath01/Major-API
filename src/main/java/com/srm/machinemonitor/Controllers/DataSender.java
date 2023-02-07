package com.srm.machinemonitor.Controllers;

import com.srm.machinemonitor.Models.Requests.WSRequest;
import com.srm.machinemonitor.Models.Tables.Data;
import com.srm.machinemonitor.Models.Tables.Machines;
import com.srm.machinemonitor.Services.DataDAO;
import com.srm.machinemonitor.Services.MachinesDAO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/fetch")
@CrossOrigin
public class DataSender {

    @Autowired
    private DataDAO dataDAO;

    @Autowired
    private MachinesDAO machineDAO;

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
                                                                @RequestParam(value="sensorName", required = true)String sensor){
        if (endDateTime == null){
            endDateTime = LocalDateTime.now();
        }
        List<Data> datas = dataDAO.getDataBetweenTimeWithSensor(machineName, startDateTime, endDateTime, sensor);
        System.out.println("machineName sensor/data " + machineName);
        System.out.println("start sensor/data " + startDateTime);
        System.out.println("end sensor/data " + endDateTime);
        // Returns date time in utc format
//        Pre-process data and give it as X and Y
        if (datas.size() > 0){
            Map res = new HashMap();
            List<LocalDateTime> X = new ArrayList<>();
            List<Double> Y = new ArrayList<>();

            for (Data d : datas){
                X.add(d.getDate());
                Y.add(Double.parseDouble(d.getValue()));
            }

            res.put("x",X);
            res.put("y",Y);
            res.put("xAxis", "DateTime");
            res.put("yAxis", datas.get(0).getData_type());
            return new ResponseEntity(res, HttpStatus.OK);
        }
        return new ResponseEntity(datas, HttpStatus.OK);
    }

    @GetMapping("/machineNames")
    public ResponseEntity<List<String>> sendMachinesAvailable(){
        System.out.println(machineDAO.getAllMachineNames());
        List<Map<String, String>> data = new ArrayList<>();
        for (Machines m : machineDAO.getAllMachineNames()){
            final Map<String, String> temp = new HashMap<>();
            temp.put("machineName", m.getMachineName());
            temp.put("sensorType", m.getSensorType());
            data.add(temp);
        }
        System.out.println(new JSONArray(data));
        return new ResponseEntity(new JSONArray(data).toString(), HttpStatus.OK);
    }

    @GetMapping("/csv")
    public void sendAsCSV(@RequestParam("machinename") String machinename, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        String currentdate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

        String headerkey = "Content-Disposition";
        String headervalue = "attachment; filename=" + machinename + currentdate + ".csv";

        response.setHeader(headerkey, headervalue);

        List<Data> data = dataDAO.getAllDataofMachineName(machinename);

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
