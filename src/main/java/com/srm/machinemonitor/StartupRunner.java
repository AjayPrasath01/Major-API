package com.srm.machinemonitor;

import com.srm.machinemonitor.Models.Tables.Organizations;
import com.srm.machinemonitor.Models.Tables.Users;
import com.srm.machinemonitor.Services.OrganizationDAO;
import com.srm.machinemonitor.Services.UsersDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private UsersDAO usersDAO;

    @Autowired
    private OrganizationDAO organizationDAO;

    @Autowired
    PasswordEncoder passwordEncoder;;

    @Override
    public void run(String... args) throws Exception {
        Users users = usersDAO.findByUserName("admin");
        if (users == null){
            users = new Users(1, "admin", passwordEncoder.encode("admin"), "admin", true, 1);
            usersDAO.save(users);
            Organizations organizations = new Organizations(1, "DevCompany", true);
            organizationDAO.save(organizations);
        }
    }
}
