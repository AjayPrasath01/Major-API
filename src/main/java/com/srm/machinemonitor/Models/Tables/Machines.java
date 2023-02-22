package com.srm.machinemonitor.Models.Tables;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@lombok.Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Machines {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    @NonNull
    String machineName;

    @Column
    @NonNull
    String secert;

    @Column
    @NonNull
    String sensors;

    @Column(name="organizationId")
    int organizationId;

    @OneToMany(targetEntity = Data.class, cascade = CascadeType.ALL)
    @JoinColumn(name="machineId", referencedColumnName = "id")
    List<Data> data;

}
