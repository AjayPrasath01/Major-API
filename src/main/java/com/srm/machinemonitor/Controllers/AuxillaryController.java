package com.srm.machinemonitor.Controllers;

import com.srm.machinemonitor.CustomExceptions.UnauthorizedException;
import com.srm.machinemonitor.CustomExceptions.UserNotFoundException;
import com.srm.machinemonitor.Models.Other.CustomUserDetails;
import com.srm.machinemonitor.Models.Requests.*;
import com.srm.machinemonitor.Models.Tables.Machines;
import com.srm.machinemonitor.Models.Tables.NewUsers;
import com.srm.machinemonitor.Models.Tables.Organizations;
import com.srm.machinemonitor.Models.Tables.Users;
import com.srm.machinemonitor.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import  java.util.UUID;

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
        int organization_id = ((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId();

        Organizations organization = organizationDAO.getNameById(organization_id);
        res.put("message", principal.getName());
        res.put("username", principal.getName());
        res.put("organization", organization.getName());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PatchMapping("/login/changePassword")
    public ResponseEntity<Map<String, String>> changePassword(Principal principal, @RequestBody @Valid ChangePasswordBody changePasswordBody) throws UnauthorizedException {
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
        Integer organization_id = organizationDAO.getIdByName(changePasswordBody.getOrganization());
        if (principal == null){
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        if (organization_id == null){
            res.put("message", "Organization not found");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        if (!organization_id.equals(((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId())){
            res.put("message", "Unauthorized");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        if (!Objects.equals(changePasswordBody.getUsername(), principal.getName())){
            throw new UnauthorizedException("Illegal Access");
        }
        Users users = usersDAO.findByUsername(principal.getName());
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
    public ResponseEntity<Map<String, String>> addUsers(@RequestBody @Valid NewUserRequest newUser, Principal principal){
        res = new HashMap<>();
        Integer organization_id = organizationDAO.getIdByName(newUser.getOrganization());
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
        if (usersDAO.existsByUsernameAndOrganizationId(newUser.getUsername(), organization_id)) {
            res.put("message", "Username Already Taken");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }
        Users users = new Users();
        users.setUsername(newUser.getUsername());
        users.setPassword(passwordEncoder.encode(newUser.getPassword()));
        users.setIsActive(true);
        users.setRole("admin");
        users.setOrganizationId(organization_id);
//        newUsersDAO.save(newUser);
        System.out.print("New user : ");
        System.out.println(users);
        usersDAO.save(users);
        res.put("message", "Request Received");

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/register/new/user")
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

//    @PostMapping("/allowUsers")
//    public ResponseEntity<Map<String, String>> allowUser(@RequestBody @Valid AllowUserRequest allowUser, @RequestParam(value = "role", required = false, defaultValue="visitor") String role) throws UserNotFoundException {
//        /*
//        PUT
//        Example :
//        {
//            "username": "ajay",
//            "organization": "dev",
//          }
//         */
//        res = new HashMap<>();
//        Users user = usersDAO.findByUsernameAndOrganizationName(allowUser.getUsername(), allowUser.getOrganization());
//        if (user == null){
//            throw new UserNotFoundException("The selected user is missing missing");
//        }
//        if (user.getIsActive()){
//            res.put("message", "Permission Already Granded");
//            return new ResponseEntity<>(res, HttpStatus.OK);
//        }
//        user.setIsActive(true);
//        usersDAO.save(user);
//        res.put("message", "Permission Granded");
//        return new ResponseEntity<>(res, HttpStatus.OK);
//    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<Map<String, String>> removeNewUser(@RequestBody @Valid AllowUserRequest userRequest, Principal principal) throws UserNotFoundException{
        /*
        DELETE
        Example :
        {
            "username": "ajay",
            "organization": "dev"
          }
         */
        res = new HashMap<>();
        if (principal == null){
            res.put("message", "Missing authentication");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        if(!((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getRole().equals("admin")){
            res.put("message", "Unauthorized only admin allowed");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        Integer organization_id = organizationDAO.getIdByName(userRequest.getOrganization());
        if (organization_id != null && organization_id != ((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId()){
            res.put("message", "unauthorized");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersDAO.findByUsernameAndOrganizationName(userRequest.getUsername(), userRequest.getOrganization());
        usersDAO.delete(user);
        res.put("message", "Removed the user");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/getUsers")
    public ResponseEntity<List<Users>> getNewUsers(@RequestParam(name="organization", required = true) String organization, Principal principal){
        res = new HashMap<>();
        Integer organization_id = organizationDAO.getIdByName(organization);
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

//    @GetMapping("/getCurrentUsers")
//    public ResponseEntity<List<Users>> getCurrentUsers(){
//        return new ResponseEntity<>(usersDAO.findAll(), HttpStatus.OK);
//    }

//    @DeleteMapping("/deleteCurrentUser")
//    public ResponseEntity<Map<String, String>> removeCurrentUser(@RequestBody Users nUser) throws UserNotFoundException{
//        /*
//        DELETE
//        Example :
//        {
//            "username": "ajay",
//            "password": "a6c9450f835ff05ce70acd259c74420bf3c3734e76d5fec12aeb6021cdc452e6874029f8419d487e",
//            "details": "Student",
//            "id": "1" // important
//          }
//         */
//
//        res = new HashMap<>();
//        Users user = usersDAO.findByUsername(nUser.getUsername());
//
//        if (user == null){
//            throw new UserNotFoundException("The select user when missing");
//        }
//        usersDAO.delete(user);
//        res.put("message", "Removed successfully");
//        return new ResponseEntity<>(res, HttpStatus.OK);
//    }

    //If a device is added
    @PostMapping("/addDevice")
    public ResponseEntity<Resource> addDevice(@RequestBody AddDeviceRequest addDeviceRequest, Principal principal) throws IOException {
        /*
        During add device it checks for exsisting name and return error if the name alrady exists
         */
        res = new HashMap<>();
        if (principal == null){
            res.put("message", "Missing authentication");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        Integer organization_id = organizationDAO.getIdByName(addDeviceRequest.getOrganization());
        if (organization_id != null && !organization_id.equals(((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId())){
            res.put("message", "Unauthorized");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        if (organization_id == null){
            res.put("message", "Organization do not exists");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        if(!((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getRole().equals("admin")){
            res.put("message", "Unauthorized only admin allowed");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        if (machinesDAO.existsBymachineName(addDeviceRequest.getMachineName())){
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

    @GetMapping("/login/status")
    public ResponseEntity loginStaus(Principal principal){
        final Map<String, String> res = new HashMap<>();
        if (principal == null){
            res.put("message", "Not logged in");
            return new ResponseEntity(res, HttpStatus.UNAUTHORIZED);
        }
        res.put("message", "logged in");
        int organization_id = ((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getOrganizationId();

        Organizations organization = organizationDAO.getNameById(organization_id);
        res.put("username", principal.getName());
        res.put("organization", organization.getName());
        return new ResponseEntity(res, HttpStatus.OK);
    }
}
