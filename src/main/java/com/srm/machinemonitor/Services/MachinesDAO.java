package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Tables.Machines;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
@Transactional
public interface MachinesDAO extends JpaRepository<Machines, Integer> {

    boolean existsBymachineNameAndOrganizationId(String machineName, int organizationId);

//    @Query(value="SELECT DISTINCT machines.machine_name FROM machines", nativeQuery = true)
//    Machines getAllMachineNames();

    Boolean existsByOrganizationIdAndMachineNameAndSecert(int organizationId, String machineName, String secert);

    List<Machines> findAllByOrganizationIdOrderByMachineNameAsc(int organizationId);

    Machines getIdByMachineNameAndSensorsAndOrganizationId(String machineName, String sensor, int orgnaizatonId);

    Machines findByMachineNameAndSensorsAndOrganizationId(String machineName, String sensor, int organizationId);

    @Query(value="SELECT machines.sensor_type FROM machines WHERE machines.machine_name = ?1", nativeQuery = true)
    String getChartType(String machinenmame);


    @Query(value="SELECT * FROM machines WHERE machines.machineName = ?1 AND machines.organizationId = ?2 ORDER BY machines.machineName ASC", nativeQuery = true)
    List<Machines> findByMachineNamesAndOrganizationId(String machineNames, int organizationId);
}
