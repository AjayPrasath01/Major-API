package com.srm.machinemonitor;

import com.srm.machinemonitor.Models.Tables.Organizations;
import com.srm.machinemonitor.Models.Tables.SuperAdmins;
import com.srm.machinemonitor.Models.Tables.Users;
import com.srm.machinemonitor.Services.SuperAdminsDAO;
import com.srm.machinemonitor.Services.UsersDAO;
import com.srm.machinemonitor.Services.OrganizationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    @Value("${defaultUsersname}")
    String DefaultUsername;

    @Value("${defaultPassword}")
    String DefaultUserPassword;

    @Value("${defaultOrganization}")
    String DefaultOrganization;

    @Value("${defaultSuperAdmin}")
    String DefaultSuperAdminName;

    @Value("${defaultSuperAdminPassword}")
    String DefaultSuperAdminPassword;

    @Autowired
    private UsersDAO usersDAO;

    @Autowired
    private OrganizationDAO organizationDAO;

    @Autowired
    PasswordEncoder passwordEncoder;;

    @Autowired
    SuperAdminsDAO superAdminsDAO;

    @Override
    public void run(String... args) throws Exception {
        Users users = usersDAO.findByUsernameAndOrganizationName(DefaultUsername, DefaultOrganization);
        SuperAdmins superAdmin = superAdminsDAO.findByUsername(DefaultSuperAdminName);
        Organizations organizations = organizationDAO.findByName(DefaultOrganization);
        if (organizations == null){
            organizations = new Organizations(1, DefaultOrganization, true);
            organizationDAO.save(organizations);
        }
        if (users == null){
            users = new Users(1, DefaultUsername, passwordEncoder.encode(DefaultUserPassword), "admin", true, 1);
            usersDAO.save(users);
        }
        if (superAdmin == null){
            superAdmin = new SuperAdmins(1, DefaultSuperAdminName, passwordEncoder.encode(DefaultSuperAdminPassword));
            superAdminsDAO.save(superAdmin);
        }
        System.out.println("Default user details : UserName : " + users.getUsername() + " Password : " + users.getPassword() + " Organization : " + organizations.getName());
        System.out.println("Default user details : UserName : " + users.getUsername() + " Password : " + users.getPassword());
    }
}
