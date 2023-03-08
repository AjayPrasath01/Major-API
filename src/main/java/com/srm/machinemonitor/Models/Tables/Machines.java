package com.srm.machinemonitor.Models.Tables;

import lombok.*;

import javax.persistence.*;
import java.math.BigInteger;
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
    private BigInteger id;

    @Column
    @NonNull
    private String machineName;

    @Column
    @NonNull
    private String secert;

    @Column
    @NonNull
    private String sensors;

    @Column(name="organizationId")
    @NonNull
    private BigInteger organizationId;

    @Column
    @NonNull
    String mode = "dev";

    @OneToMany(targetEntity = Data.class, cascade = CascadeType.ALL)
    @JoinColumn(name="machineId", referencedColumnName = "id")
    List<Data> data;

}
