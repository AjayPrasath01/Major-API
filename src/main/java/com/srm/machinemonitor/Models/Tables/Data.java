package com.srm.machinemonitor.Models.Tables;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

@Entity
@AllArgsConstructor
@lombok.Data
@NoArgsConstructor
@ToString
public class Data {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NonNull
    @Column
    public LocalDateTime date;

    @NonNull
    @Column
    public String data_type;

    @NonNull
    @Column
    public String value;

    @NonNull
    @Column
    int machineId;

}
