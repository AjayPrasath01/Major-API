package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Tables.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsersDAO extends JpaRepository<Users, Integer> {

    @Query(value="SELECT * FROM users WHERE users.username = ?1", nativeQuery = true)
    Users findByUserName(String username);
}
