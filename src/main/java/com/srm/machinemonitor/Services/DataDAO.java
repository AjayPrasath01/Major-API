package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Tables.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface DataDAO extends JpaRepository<Data, Integer> {

    @Query(value="SELECT DISTINCT data.machine_name FROM data", nativeQuery = true)
    List<String> getAllMachineNames();

    @Query(value="SELECT COUNT(*) FROM data WHERE data.machineId = ?1 AND data.data_type <> ?2", nativeQuery = true)
    Integer countByMachineIDAndDatatype(int machineId, String dataType);

    @Query(value="SELECT * FROM data WHERE data.date >= ?2 AND data.date <= ?3 AND data.machine_name = ?1", nativeQuery = true)
    List<Data> getDataBetweenTime(String deviceName, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value="SELECT * FROM data WHERE data.date >= ?2 AND data.date <= ?3 AND data.machineId = ?1", nativeQuery = true)
    List<Data> getDataBetweenTimeWithSensor(int machineId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value="SELECT * FROM data WHERE data.date >= ?1 AND data.machineId = ?2", nativeQuery = true)
    List<Data> getDataFromDate(LocalDateTime startTime, int machineId);

    @Query(value="SELECT * FROM data WHERE data.date > ?1 AND data.machine_name = ?2 AND data.sensor_name = ?3 ORDER BY data.date ASC", nativeQuery = true)
    List<Data> getDataFromDateSensor(LocalDateTime startTime, String machinename, String sensorName);

//    @Query(value="SELECT * FROM data WHERE data.machine_name = ?1 ORDER BY data.date DESC", nativeQuery = true)
//    List<Data> getAllDataofMachineName(String machinename);

    List<Data> findAllByMachineId(int machineId);

    @Modifying
    void deleteAllByMachineId(Integer machineId);

    @Query(value="SELECT DISTINCT data.sensor_name FROM data WHERE data.machine_name = ?1", nativeQuery = true)
    List<String> finAllSensorsByMachineName(String machinename);
}
