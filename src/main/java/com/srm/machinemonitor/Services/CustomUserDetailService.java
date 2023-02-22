package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Other.CustomUserDetails;
import com.srm.machinemonitor.Models.Tables.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UsersDAO usersDAO;

    @Override
    public UserDetails loadUserByUsername(String username) {
        String[] temp = username.split(":");
        Users users = usersDAO.findByUsernameAndOrganizationName(temp[1], temp[0]);
        System.out.println("loadUserByUsername arg " + username);
        if (users == null){
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(users);
    }
}
