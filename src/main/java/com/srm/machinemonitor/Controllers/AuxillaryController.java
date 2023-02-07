package com.srm.machinemonitor.Controllers;

import com.srm.machinemonitor.CustomExceptions.UnauthorizedException;
import com.srm.machinemonitor.CustomExceptions.UserNotFoundException;
import com.srm.machinemonitor.Models.Requests.AddDeviceRequest;
import com.srm.machinemonitor.Models.Requests.ChangePasswordBody;
import com.srm.machinemonitor.Models.Requests.RemoveMachineRequest;
import com.srm.machinemonitor.Models.Tables.Machines;
import com.srm.machinemonitor.Models.Tables.NewUsers;
import com.srm.machinemonitor.Models.Tables.Users;
import com.srm.machinemonitor.Services.DataDAO;
import com.srm.machinemonitor.Services.MachinesDAO;
import com.srm.machinemonitor.Services.NewUsersDAO;
import com.srm.machinemonitor.Services.UsersDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.security.Principal;
import java.util.*;

@RestController
@CrossOrigin
public class AuxillaryController {

    Map<String, String> res;
    Random random = new Random();

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    NewUsersDAO newUsersDAO;

    @Autowired
    UsersDAO usersDAO;
    
    @Autowired
    MachinesDAO machinesDAO;

    @Autowired
    DataDAO dataDAO;


    @RequestMapping("/")
    public ResponseEntity<Map<String, String>> home(HttpSession session, CsrfToken csrfToken){
        res = new HashMap<>();
        res.put("session", session.getId());
        res.put("xsrf-token", csrfToken.getToken());
        return new ResponseEntity(res, HttpStatus.OK);
    }

    @RequestMapping("/user")
    public ResponseEntity<Map<String, String>> getUsername(Principal principal){
        res = new HashMap<>();
        if (principal == null){
            res.put("message", null);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        res.put("message", principal.getName());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PatchMapping("/login/changePassword")
    public ResponseEntity<Map<String, String>> changePassword(Principal principal, @RequestBody ChangePasswordBody changePasswordBody) throws UnauthorizedException {
        /*
        To Change Password
        PATCH
        Example :
        {
            "username": "admin",
            "currentPassword": "admin",
            "newPassword": "password"
        }
         */
        res = new HashMap<>();
        if (!Objects.equals(changePasswordBody.getUsername(), principal.getName())){
            throw new UnauthorizedException("Illegal Access");
        }
        Users users = usersDAO.findByUserName(principal.getName());

        if (passwordEncoder.matches(changePasswordBody.getCurrentPassword(), users.getPassword())){
            users.setPassword(passwordEncoder.encode(changePasswordBody.getNewPassword()));
            usersDAO.save(users);
        }else{
            throw new UnauthorizedException("Wrong Password");
        }
        res.put("message", "Password Changed Successfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/addUsers")
    public ResponseEntity<Map<String, String>> addUsers(@RequestBody NewUsers newUser){
        res = new HashMap<>();
        if (newUsersDAO.existsByUsername(newUser.getUsername())) {
            res.put("message", "Username Already Taken");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUsersDAO.save(newUser);
        res.put("message", "Request Received");

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/allowUsers")
    public ResponseEntity<Map<String, String>> allowUser(@RequestBody NewUsers nUser, @RequestParam(value = "role", required = false, defaultValue="visitor") String role) throws UserNotFoundException {
        /*
        PUT
        Example :
        {
            "username": "ajay",
            "password": "a6c9450f835ff05ce70acd259c74420bf3c3734e76d5fec12aeb6021cdc452e6874029f8419d487e",
            "details": "Student",
            "id": "1" // important
          }
         */
        res = new HashMap<>();
        NewUsers newUser = newUsersDAO.findById(nUser.getId()).orElse(null);
        if (newUser == null){
            throw new UserNotFoundException("The select user when missing");
        }
        newUsersDAO.deleteById(nUser.getId());
        Users users = new Users(newUser.getUsername(), newUser.getPassword(), role);
        usersDAO.save(users);
        res.put("message", "Permission Granded");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @DeleteMapping("/removeNewUsers")
    public ResponseEntity<Map<String, String>> removeNewUser(@RequestBody NewUsers nUser) throws UserNotFoundException{
        /*
        DELETE
        Example :
        {
            "username": "ajay",
            "password": "a6c9450f835ff05ce70acd259c74420bf3c3734e76d5fec12aeb6021cdc452e6874029f8419d487e",
            "details": "Student",
            "id": "1" // important
          }
         */
        res = new HashMap<>();
        NewUsers newUser = newUsersDAO.findById(nUser.getId()).orElse(null);
        if (newUser == null){
            throw new UserNotFoundException("The select user when missing");
        }
        newUsersDAO.delete(newUser);
        res.put("message", "Removed the user");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/getNewUsers")
    public ResponseEntity<List<NewUsers>> getNewUsers(){
        res = new HashMap<>();
        return new ResponseEntity<>(newUsersDAO.findAll(), HttpStatus.OK);
    }

    @GetMapping("/getCurrentUsers")
    public ResponseEntity<List<Users>> getCurrentUsers(){
        return new ResponseEntity<>(usersDAO.findAll(), HttpStatus.OK);
    }

    @DeleteMapping("/deleteCurrentUser")
    public ResponseEntity<Map<String, String>> removeCurrentUser(@RequestBody Users nUser) throws UserNotFoundException{
        /*
        DELETE
        Example :
        {
            "username": "ajay",
            "password": "a6c9450f835ff05ce70acd259c74420bf3c3734e76d5fec12aeb6021cdc452e6874029f8419d487e",
            "details": "Student",
            "id": "1" // important
          }
         */

        res = new HashMap<>();
        Users user = usersDAO.findByUserName(nUser.getUsername());

        if (user == null){
            throw new UserNotFoundException("The select user when missing");
        }
        usersDAO.delete(user);
        res.put("message", "Removed successfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    //If a device is added
    @PostMapping("/addDevice")
    public ResponseEntity<Resource> addDevice(@RequestBody AddDeviceRequest addDeviceRequest) throws IOException {
        /*
        During add device it checks for exsisting name and return error if the name alrady exists
         */
        res = new HashMap<>();
        if (machinesDAO.existsBymachineName(addDeviceRequest.getMachineName())){
            res.put("message", "Machine Name already Taken");
            return new ResponseEntity(res, HttpStatus.CONFLICT);
        }
        Machines machines = new Machines();
        machines.setMachineName(addDeviceRequest.getMachineName());
        machines.setSecert(random.nextInt(1, 1000));
        machines.setSensorType(addDeviceRequest.getSensorType());
        machinesDAO.save(machines);

        File file = new File("ArduinoSD.ino");

//        String line = null;
//        List<String> lines = new ArrayList<>();
//
//        FileReader reader = new FileReader(file);
//
//        BufferedReader bufferedReader = new BufferedReader(reader);
//        while ((line = bufferedReader.readLine()) != null){
//            if (line.contains("SSIDNAME")){
//                line = line.replaceAll("SSIDNAME", "\"" + addDeviceRequest.getSsid() + "\"");
//            }else if (line.contains("SSIDPASSWORD")){
//                line = line.replaceAll("SSIDPASSWORD", "\"" + addDeviceRequest.getPassword() + "\"");
//            }else if (line.contains("IMPORTANT")){
//                line = line.replaceAll("IMPORTANT", "\"" + addDeviceRequest.getMachineName() + "\"");
//            }
//            lines.add(line);
//        }
//        bufferedReader.close();
//        reader.close();
//
////        System.out.println(lines);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//        DataOutputStream out = new DataOutputStream(baos);
//
//        for (String s: lines){
//            baos.write(s.getBytes());
//        }
//        byte[] bytes = baos.toByteArray();
//
//        ByteArrayResource baseResource = new ByteArrayResource(bytes);
//        System.out.println(baos);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
//        System.out.println(temp);

        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
//        return new ResponseEntity(baseResource, HttpStatus.OK);
    }
    
    @GetMapping("/checkNewMachineName")
    public ResponseEntity<Map<String, String>> checkDeviceName(@RequestParam(value="machineName") String machinename){
        res = new HashMap<>();
        if (machinesDAO.existsBymachineName(machinename)){
            res.put("message", "Device Name is already taken");
            return new ResponseEntity<>(res, HttpStatus.NOT_ACCEPTABLE);
        }
        res.put("message", "Valid name");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @DeleteMapping("/removeMachine")
    public ResponseEntity<Map<String, String>> removeMachine(@RequestBody RemoveMachineRequest removeMachineRequest){
        res = new HashMap<>();
        dataDAO.deleteAllByMachinenames(removeMachineRequest.getMachineName());
        machinesDAO.deleteByMachineNames(removeMachineRequest.getMachineName());
        res.put("message", "Removed the device successfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/sensors")
    public ResponseEntity<List<String>> sendSensor(@RequestParam("machineName")String machineName){
        return new ResponseEntity<>(dataDAO.finAllSensorsByMachineName(machineName), HttpStatus.OK);
    }
}
