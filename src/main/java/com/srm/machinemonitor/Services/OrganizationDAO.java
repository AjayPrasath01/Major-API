package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Tables.Organizations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;

public interface OrganizationDAO extends JpaRepository<Organizations, BigInteger> {
    Organizations findByName(String name);

    @Query(value="SELECT id from organizations where name = ?1",nativeQuery = true)
    BigInteger getIdByName(String name);

    Organizations getNameById(BigInteger id);

    boolean existsByName(String organization);
}
