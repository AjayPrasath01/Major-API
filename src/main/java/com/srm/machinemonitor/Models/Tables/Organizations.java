package com.srm.machinemonitor.Models.Tables;

import lombok.*;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Data
public class Organizations {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    int id;

    @Column
    @NonNull
    String name;

    @Column(columnDefinition = "boolean default true")
    Boolean isActive;

//    @OneToMany(targetEntity = Users.class, cascade = CascadeType.ALL)
//    @JoinColumn(name="ordanization_id", referencedColumnName = "id")
//    private List<Users> users;

    @OneToMany(targetEntity = Machines.class, cascade = CascadeType.ALL)
    @JoinColumn(name="ordanization_id", referencedColumnName = "id")
    private List<Machines> machines;

    public Organizations(int id, String name, boolean isActive){
        this.id = id;
        this.name = name;
        this.isActive = isActive;
    }
}
