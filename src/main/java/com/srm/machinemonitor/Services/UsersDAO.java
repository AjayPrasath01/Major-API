package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Tables.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UsersDAO extends JpaRepository<Users, Integer> {

//    @Query(value="SELECT * FROM users WHERE users.username = ?1", nativeQuery = true)
    @Query(value="SELECT * from users where users.organizationId = (SELECT id FROM organizations WHERE organizations.name = ?2) AND users.username = ?1", nativeQuery = true)
    Users findByUsernameAndOrganizationName(String username, String OrganizationName);

    Boolean existsByUsernameAndOrganizationId(String username, int organization_id);
    // To remove
    Users findByUsername(String name);

    List<Users> findAllByOrganizationId(int organization_is);

//    Users findByUsernameAndOrganizationId(String username, int organization_id);
}
