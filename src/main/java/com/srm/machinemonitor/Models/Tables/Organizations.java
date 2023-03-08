package com.srm.machinemonitor.Models.Tables;

import lombok.*;
import lombok.Data;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
@Getter
@Setter
@Data
public class Organizations {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    BigInteger id;

    @Column(unique=true)
    @NonNull
    String name;

    @Column(columnDefinition = "boolean default true")
    Boolean isActive;

    @ToString.Exclude
    @OneToMany(targetEntity = Users.class, cascade = CascadeType.ALL)
    @JoinColumn(name="organizationId", referencedColumnName = "id")
    private List<Users> users = new java.util.ArrayList<>();

    @ToString.Exclude
    @OneToMany(targetEntity= Machines.class, cascade = CascadeType.ALL)
    @JoinColumn(name="organizationId", referencedColumnName = "id")
    private List<Machines> machines = new java.util.ArrayList<>();

    @ToString.Exclude
    @OneToMany(targetEntity= Log.class, cascade = CascadeType.ALL)
    @JoinColumn(name="organizationId", referencedColumnName = "id")
    private List<Log> logs = new java.util.ArrayList<>();

    public Organizations(BigInteger id, String name, boolean isActive){
        this.id = id;
        this.name = name;
        this.isActive = isActive;
    }
}
