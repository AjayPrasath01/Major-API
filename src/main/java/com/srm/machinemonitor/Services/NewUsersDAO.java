package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Tables.NewUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NewUsersDAO extends JpaRepository<NewUsers, Integer> {

    NewUsers findByUsername(String username);

    List<NewUsers> findAll();

    boolean existsByUsername(String username);
}
