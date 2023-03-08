package com.srm.machinemonitor.Services;

import com.srm.machinemonitor.Models.Other.BaseData;
import com.srm.machinemonitor.Models.Tables.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface DataDAO extends JpaRepository<Data, BigInteger> {

    @Query(value="SELECT COUNT(*) FROM data WHERE data.machineId = ?1 AND data.data_type <> ?2", nativeQuery = true)
    Integer countByMachineIDAndDatatype(BigInteger machineId, String dataType);

    @Query(value="SELECT * FROM data WHERE data.date >= ?2 AND data.date <= ?3 AND data.machineId = ?1 ORDER BY data.date ASC", nativeQuery = true)
    List<Data> getDataBetweenTimeWithMachineId(BigInteger machineId, LocalDateTime startDate, LocalDateTime endDate);

    List<Data> findAllByMachineIdAndDateGreaterThanOrderByDateAsc(BigInteger machineId, LocalDateTime date);

    List<BaseData> findAllByMachineId(BigInteger machineId);

    @Modifying
    void deleteAllByMachineId(BigInteger machineId);

    @Query(value="SELECT DISTINCT data.sensor_name FROM data WHERE data.machine_name = ?1", nativeQuery = true)
    List<String> finAllSensorsByMachineName(String machinename);
}
