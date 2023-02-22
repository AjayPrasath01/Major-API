package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Tables.Organizations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrganizationDAO extends JpaRepository<Organizations, Integer> {
    Organizations findByName(String name);

    @Query(value="SELECT id from organizations where name = ?1",nativeQuery = true)
    Integer getIdByName(String name);

    Organizations getNameById(int id);

    boolean existsByName(String organization);
}
