package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Tables.Organizations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationDAO extends JpaRepository<Organizations, Integer> {
}
