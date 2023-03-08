package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Tables.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.List;

public interface UsersDAO extends JpaRepository<Users, BigInteger> {

//    @Query(value="SELECT * FROM users WHERE users.username = ?1", nativeQuery = true)
    @Query(value="SELECT * from users where users.organizationId = (SELECT id FROM organizations WHERE organizations.name = ?2) AND users.username = ?1", nativeQuery = true)
    Users findByUsernameAndOrganizationName(String username, String OrganizationName);

    Boolean existsByUsernameAndOrganizationId(String username, BigInteger organization_id);

    Users findByUsernameAndOrganizationId(String name, BigInteger organization_id);

    @Query(value="SELECT COUNT(*) FROM Users WHERE Users.organizationId = ?1 AND Users.role = ?2 AND Users.isActive = ?3", nativeQuery = true)
    Integer countByOrganizationIdAndRoleAndIsActive(BigInteger organizationId, String role, boolean isActive);

    List<Users> findAllByOrganizationId(BigInteger organization_is);

//    Users findByUsernameAndOrganizationId(String username, int organization_id);
}
