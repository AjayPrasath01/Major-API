package com.srm.machinemonitor.Models.Tables;

import lombok.*;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
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
    @Column
    @NotEmpty(message="username can't be empty")
    String username;

    @NonNull
    @Column
    @NotEmpty(message="password can't be empty")
    String password;

    @NonNull
    @Column
    @NotEmpty(message="email can't be empty")
    String email;

    @NonNull
    @Column
    String role;

    @Column(columnDefinition="boolean default true")
    Boolean isActive;

    @Column(name="organizationId")
    int organizationId;

    @ManyToOne(targetEntity = Organizations.class, cascade = CascadeType.ALL)
    List<Users> users;

    public Users(int id, String username, String password, String role, boolean isActive, int ordanization_id){
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
        this.organizationId = ordanization_id;
    }
}
