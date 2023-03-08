package com.srm.machinemonitor.Controllers;

import com.srm.machinemonitor.CustomExceptions.UnauthorizedException;
import com.srm.machinemonitor.CustomExceptions.UserNotFoundException;
import com.srm.machinemonitor.Models.Other.CustomUserDetails;
import com.srm.machinemonitor.Models.Requests.*;
import com.srm.machinemonitor.Models.Tables.Log;
import com.srm.machinemonitor.Models.Tables.Machines;
import com.srm.machinemonitor.Models.Tables.Organizations;
import com.srm.machinemonitor.Models.Tables.Users;
import com.srm.machinemonitor.Services.*;
import com.srm.machinemonitor.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import  java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.*;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin
public class AuxillaryController {

    Map<String, String> res;

    @Autowired
    LogDAO logDAO;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UsersDAO usersDAO;
    
    @Autowired
    MachinesDAO machinesDAO;

    @Autowired
    DataDAO dataDAO;

    @Autowired
    OrganizationDAO organizationDAO;


    @RequestMapping("")
    public ResponseEntity<Map<String, String>> home(HttpSession session, CsrfToken csrfToken){
        res = new HashMap<>();
        res.put("session", session.getId());
        res.put("xsrf-token", csrfToken.getToken());
        return new ResponseEntity(res, HttpStatus.OK);
    }

    @RequestMapping("/user")
    public ResponseEntity<Map<String, String>> getUsername(Principal principal){
        res = new HashMap<>();
        System.out.println(principal);
        if (principal == null){
            res.put("message", null);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        BigInteger organization_id = ((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId();

        Organizations organization = organizationDAO.getNameById(organization_id);
        res.put("message", principal.getName());
        res.put("username", principal.getName());
        res.put("organization", organization.getName());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PatchMapping("/login/changePassword")
    public ResponseEntity<Map<String, String>> changePassword(HttpServletResponse response, Principal principal, @RequestBody @Valid ChangePasswordBody changePasswordBody) throws UnauthorizedException, IOException {
        /*
        To Change Password
        PATCH
        Example :
        {
            "organization": "dev",
            "username": "admin",
            "currentPassword": "admin",
            "newPassword": "password"
        }
         */
        res = new HashMap<>();
        Map data = Utils.verifyOrgnaization(response, principal, changePasswordBody.getOrganization(), organizationDAO);
        if (data == null){
            return null;
        }
        BigInteger organization_id = (BigInteger) data.get("organizationId");
        Users users = usersDAO.findByUsernameAndOrganizationId(principal.getName(), organization_id);
        if (!principal.getName().equals(users.getUsername())){
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        if (!users.getIsActive()){
            res.put("message", "User is blocked");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        if (passwordEncoder.matches(changePasswordBody.getCurrentPassword(), users.getPassword())){
            users.setPassword(passwordEncoder.encode(changePasswordBody.getNewPassword()));
            usersDAO.save(users);
        }else{
            throw new UnauthorizedException("Wrong Password");
        }
        res.put("message", "Password Changed Successfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    //Adding and unblocking user within organization
    @PostMapping("/unblockUsers")
    public ResponseEntity<Map<String, String>> addUsers(@RequestBody @Valid UnBlockUserRequest unBlockUserRequest, Principal principal){
        res = new HashMap<>();

        BigInteger organization_id = organizationDAO.getIdByName(unBlockUserRequest.getOrganization());
        if (principal == null){
            res.put("message", "Unauthorized Cookies");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        if (organization_id == null){
            res.put("message", "Organization not found");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        if(!((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getRole().equals("admin")){
            res.put("message", "Unauthorized only admin allowed");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        if (!organization_id.equals(((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId())){
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersDAO.findByUsernameAndOrganizationName(unBlockUserRequest.getUsername(), unBlockUserRequest.getOrganization());
        users.setIsActive(true);
        usersDAO.save(users);
        res.put("message", "User Unblocked");

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/join/organization")
    public ResponseEntity joinOrganization(@RequestBody @Valid RegiesterNewUserRequest regiesterNewUserRequest, @RequestParam(value = "role", required = true) String role){
        Organizations organization = organizationDAO.findByName(regiesterNewUserRequest.getOrganization());
        if (organization == null) {
            final Map<String, String> res = new HashMap<>();
            res.put("message", "Organization do not exists");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        if (!Objects.equals(role, "admin") && !Objects.equals(role, "visitor")){
            final Map<String, String> res = new HashMap<>();
            res.put("message", "Invalid role");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        if (usersDAO.existsByUsernameAndOrganizationId(regiesterNewUserRequest.getUsername(), organization.getId())){
            final Map<String, String> res = new HashMap<>();
            res.put("message", "Username already taken");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }
        Users users = new Users();
        users.setOrganizationId(organization.getId());
        users.setUsername(regiesterNewUserRequest.getUsername());
        users.setPassword(passwordEncoder.encode(regiesterNewUserRequest.getPassword()));
        users.setEmail(regiesterNewUserRequest.getEmail());
        users.setIsActive(true);
        users.setRole(role);
        usersDAO.save(users);
        final Map<String, String> res = new HashMap<>();
        res.put("message", "New user registered");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/register/new/user")
    @Transactional
    public ResponseEntity registerNewUser(@RequestBody @Valid RegiesterNewUserRequest regiesterNewUserRequest){
        if (organizationDAO.existsByName(regiesterNewUserRequest.getOrganization())){
            final Map<String, String> res = new HashMap<>();
            res.put("message", "Organization name already taken");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }
        Organizations organizations = new Organizations();
        organizations.setName(regiesterNewUserRequest.getOrganization());
        organizations.setIsActive(true);
        organizations = organizationDAO.save(organizations);

        System.out.println(organizations);
        Users users = new Users();
        users.setOrganizationId(organizations.getId());
        users.setUsername(regiesterNewUserRequest.getUsername());
        users.setPassword(passwordEncoder.encode(regiesterNewUserRequest.getPassword()));
        users.setEmail(regiesterNewUserRequest.getEmail());
        users.setIsActive(true);
        users.setRole("admin");
        usersDAO.save(users);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/count/data/points")
    public ResponseEntity countDataPoints(@RequestParam("machinename") String machineName, @RequestParam("organization") String organizationName, @RequestParam("sensor") String sensor, HttpServletResponse response, Principal principal) throws IOException {
        Map data = Utils.verifyOrgnaization(response, principal, organizationName, organizationDAO);
        if (data == null){
            return null;
        }

        BigInteger organizationId = (BigInteger) data.get("organizationId");

        Machines machine = machinesDAO.getIdByMachineNameAndSensorsAndOrganizationId(machineName, sensor, organizationId);
        if (machine == null){
            final Map res = new HashMap();
            res.put("message", "Machine with the selected sensor not found");
            return new ResponseEntity(res, HttpStatus.NOT_FOUND);
        }
        Integer dataPoints = dataDAO.countByMachineIDAndDatatype(machine.getId(), "status");

        return new ResponseEntity(dataPoints, HttpStatus.OK);
    }

    @DeleteMapping("/block/user")
    public ResponseEntity<Map<String, String>> removeNewUser(@RequestBody @Valid AllowUserRequest userRequest, Principal principal, HttpServletResponse response) throws UserNotFoundException, IOException {
        /*
        DELETE
        Example :
        {
            "username": "ajay",
            "organization": "dev"
          }
         */
        res = new HashMap<>();
        Map verified = Utils.verifyAdminAndOrganizationIDOR(response, principal, userRequest.getOrganization(), organizationDAO);
        if (verified == null){
            return null;
        }
        BigInteger organization_id = (BigInteger) verified.get("organizationId");
        Users user = usersDAO.findByUsernameAndOrganizationName(userRequest.getUsername(), userRequest.getOrganization());
        if (user.getRole().equals("admin")){
            Integer activeAdmins  = usersDAO.countByOrganizationIdAndRoleAndIsActive(organization_id, "admin", true);
            if (activeAdmins > 1){
                user.setIsActive(false);
                usersDAO.save(user);
                res.put("message", "User has been blocked");
                return new ResponseEntity<>(res, HttpStatus.OK);
            }else{
                res.put("message", "User can't been blocked as there are only " + activeAdmins + " left");
                return new ResponseEntity<>(res, HttpStatus.NOT_ACCEPTABLE);
            }
        }
        user.setIsActive(false);
        usersDAO.save(user);
        res.put("message", "User has been blocked");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/getUsers")
    public ResponseEntity<List<Users>> getNewUsers(@RequestParam(name="organization", required = true) String organization, Principal principal){
        res = new HashMap<>();
        BigInteger organization_id = organizationDAO.getIdByName(organization);
        if (principal == null){
            res.put("message", "unauthorization missing");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        if (organization_id == null){
            res.put("message", "unauthorized");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        if (organization_id != ((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId()){
            res.put("message", "unauthorized");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(usersDAO.findAllByOrganizationId(organization_id), HttpStatus.OK);
    }

    @GetMapping("/log/data")
    public ResponseEntity loggingIOT(@RequestParam(value = "machineName", required = true)String machineName, @RequestParam(value = "organization", required = true) String organization, @RequestParam(value = "token", required = true) String token, @RequestParam(value = "log", required = true) String logData, @RequestParam(value = "logType", defaultValue="INFO") String logType, HttpServletResponse response) throws IOException {
        res = new HashMap();
        logData = URLDecoder.decode(logData, "UTF-8");
        Map data =  Utils.verifyIotToken(response,  organization, machineName, token, machinesDAO, organizationDAO);
        final Map verify = new HashMap();
        if (data == null){
            return null;
        }
        BigInteger organizationId = (BigInteger) data.get("organizationId");
        Log logTable = new Log();
        logType = Utils.verifyLogType(logType);
        logTable.setMachineName(machineName);
        logTable.setOrganizationId(organizationId);
        LocalDateTime time = LocalDateTime.now();
        String log = time.toString() + " [" + logType + "] " + logData;
        logTable.setLog(log);
        logTable.setTime(LocalDateTime.now());
        if (logData.isBlank()){
            res.put("message", "Given data is blank");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        logDAO.save(logTable);
        res.put("message", "Given data id added");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/log/get/data")
    public ResponseEntity sendLogIOT(@RequestParam(value = "machineName", required = true)String machineName, @RequestParam(value = "organization", required = true) String organization, Principal principal, HttpServletResponse response) throws IOException {
        Map data = Utils.verifyOrgnaization(response, principal, organization, organizationDAO);
        if (data == null){
            return null;
        }
        BigInteger organizationId = (BigInteger) data.get("organizationId");
        return new ResponseEntity(logDAO.findAllLogByMachineNameAndOrganizationId(machineName, organizationId), HttpStatus.OK);
    }


    //If a device is added
    @PostMapping("/addDevice")
    @Transactional
    public ResponseEntity<Resource> addDevice(@RequestBody AddDeviceRequest addDeviceRequest, Principal principal, HttpServletResponse response) throws IOException {
        /*
        During add device it checks for exsisting name and return error if the name alrady exists
         */
        res = new HashMap<>();
        Map data = Utils.verifyOrgnaization(response, principal, addDeviceRequest.getOrganization(), organizationDAO);
        if (data == null){
            return null;
        }
        BigInteger organization_id = (BigInteger) data.get("organizationId");
        if (machinesDAO.existsBymachineNameAndOrganizationId(addDeviceRequest.getMachineName(), organization_id)){
            res.put("message", "Machine Name already Taken");
            return new ResponseEntity(res, HttpStatus.CONFLICT);
        }
        String secert = UUID.randomUUID().toString();
        String sensors[] = addDeviceRequest.getSensors().split(",");
        for (int i=0; i<sensors.length; i++){
            Machines machines = new Machines();
            machines.setMachineName(addDeviceRequest.getMachineName().toLowerCase());
            machines.setSecert(secert);
            machines.setSensors(sensors[i].toLowerCase());
            machines.setOrganizationId(organization_id);
            machinesDAO.save(machines);
        }

        String fileName = "ArduinoSD.ino";
        InputStream inputStream = new FileInputStream(fileName);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        String fileContent = outputStream.toString();
        inputStream.close();
        outputStream.close();
        // Modify the file content as needed
        fileContent  = fileContent.replace("<TOKEN>", secert);
        fileContent  = fileContent.replace("<ORGANIZATIONNAME>", addDeviceRequest.getOrganization());
        fileContent  = fileContent.replace("<MACHINENAME>", addDeviceRequest.getMachineName());
        if (addDeviceRequest.getSsid() != null && addDeviceRequest.getPassword() != null){
            fileContent  = fileContent.replace("<SSIDPASSWORD>", addDeviceRequest.getPassword());
            fileContent  = fileContent.replace("<SSIDNAME>", addDeviceRequest.getSsid());
        }

        // Send the modified file content in the response
        byte[] bytes = fileContent.getBytes();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        return ResponseEntity.ok()
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
    
    @GetMapping("/checkNewMachineName")
    public ResponseEntity<Map<String, String>> checkDeviceName(@RequestParam(value="machineName") String machinename, @RequestParam(value="organization") String organization, Principal principal, HttpServletResponse response) throws IOException {
        res = new HashMap<>();
        Map data = Utils.verifyOrgnaization(response, principal, organization, organizationDAO);
        if (data == null){
            return null;
        }
        BigInteger organization_id = (BigInteger) data.get("organizationId");
        if (machinesDAO.existsBymachineNameAndOrganizationId(machinename, organization_id)){
            res.put("message", "Device Name is already taken");
            return new ResponseEntity<>(res, HttpStatus.NOT_ACCEPTABLE);
        }
        res.put("message", "Valid name");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PutMapping("/update/sensors")
    @Transactional
    public ResponseEntity editSensor(@RequestBody @Valid ModifyDeviceRequest modifyDeviceRequest, Principal principal, HttpServletResponse response) throws IOException {
        res = new HashMap<>();
        Map data = Utils.verifyAdminAndOrganizationIDOR(response, principal, modifyDeviceRequest.getOrganization(), organizationDAO);
        if (data == null){
            return null;
        }
        modifyDeviceRequest.setSensors(modifyDeviceRequest.getSensors().toLowerCase());
        BigInteger organizationId = (BigInteger) data.get("organizationId");
        List<Machines> allSensor = machinesDAO.findByMachineNamesAndOrganizationId(modifyDeviceRequest.getMachineName(), organizationId);
        if (allSensor == null || allSensor.size() == 0){
            res.put("message", "Given machine name can be found");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        String[] sensors = modifyDeviceRequest.getSensors().split(",");
        if (Utils.hasDuplicates(sensors)){
            res.put("message", "Duplicate sensors names are not allowed");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }
        Machines stored = null;
        for (int i=0; i<allSensor.size(); i++){
            stored = allSensor.get(i);
            machinesDAO.deleteById(stored.getId());
        }
        if (sensors.length == 0 ){
            res.put("message", "Updated susccessfully");
            stored.setSensors("");
            machinesDAO.save(stored);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        for (int i=0; i<sensors.length; i++){
            if (Objects.equals(sensors[i], "")){
                continue;
            }
            stored.setSensors(sensors[i]);
            machinesDAO.save(stored);
        }
        res.put("message", "Updated susccessfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @DeleteMapping("/remove/machine")
    @Transactional
    public ResponseEntity<Map<String, String>> removeMachine(@RequestBody RemoveMachineRequest removeMachineRequest, HttpServletResponse response, Principal principal) throws IOException {
        res = new HashMap<>();
        Map data = Utils.verifyAdminAndOrganizationIDOR(response, principal, removeMachineRequest.getOrganization(), organizationDAO);
        if (data == null){
            return null;
        }
        BigInteger organizationId = (BigInteger) data.get("organizationId");
        List<Machines> allMachines = machinesDAO.findByMachineNamesAndOrganizationId(removeMachineRequest.getMachineName(), organizationId);
        if (allMachines == null || allMachines.size() == 0){
            res.put("message", "Machine not found");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        for (Machines m: allMachines){
            dataDAO.deleteAllByMachineId(m.getId());
        }
        machinesDAO.deleteAll(allMachines);
        res.put("message", "Removed the device successfully");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/sensors")
    public ResponseEntity<List<String>> sendSensor(@RequestParam("machineName")String machineName){
        return new ResponseEntity<>(dataDAO.finAllSensorsByMachineName(machineName), HttpStatus.OK);
    }

    @GetMapping("/login/status")
    public ResponseEntity loginStaus(Principal principal){
        final Map<String, String> res = new HashMap<>();
        if (principal == null){
            res.put("message", "Not logged in");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        res.put("message", "logged in");
        BigInteger organization_id = ((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId();

        Organizations organization = organizationDAO.getNameById(organization_id);
        res.put("username", principal.getName());
        res.put("organization", organization.getName());
        res.put("role", ((CustomUserDetails)((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getRole());
        return new ResponseEntity(res, HttpStatus.OK);
    }
}
