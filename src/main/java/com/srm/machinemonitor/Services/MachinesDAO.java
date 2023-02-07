package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Tables.Machines;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
@Transactional
public interface MachinesDAO extends JpaRepository<Machines, Integer> {

    boolean existsBymachineName(String machineName);

//    @Query(value="SELECT DISTINCT machines.machine_name FROM machines", nativeQuery = true)
//    Machines getAllMachineNames();

    @Query(value="SELECT * FROM machines WHERE machines.machine_name IN (SELECT DISTINCT machines.machine_name FROM machines)", nativeQuery = true)
    List<Machines> getAllMachineNames();

    @Query(value="SELECT machines.sensor_type FROM machines WHERE machines.machine_name = ?1", nativeQuery = true)
    String getChartType(String machinenmame);

//    @Query(value="SELECT DISTINCT machines.machine_name, machines.machine_name")
//    List<Machines> getAllMachineDetails();

    @Modifying
    @Query(value="DELETE FROM machines WHERE machines.machine_name = ?1", nativeQuery = true)
    void deleteByMachineNames(String machineNames);
}