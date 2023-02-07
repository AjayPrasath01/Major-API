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
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    @NonNull
    @Column(unique = true)
    String username;

    @NonNull
    @Column
    String password;

    @NonNull
    @Column
    String role;

    @Column(columnDefinition="boolean default true")
    Boolean isActive;

    @Column
    int ordanization_id;

    @ManyToOne(targetEntity = Organizations.class, cascade = CascadeType.ALL)
    List<Users> users;

    public Users(int id, String username, String password, String role, boolean isActive, int ordanization_id){
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
        this.ordanization_id = ordanization_id;
    }
}
