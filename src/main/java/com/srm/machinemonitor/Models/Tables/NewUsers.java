package com.srm.machinemonitor.Models.Tables;

import com.sun.istack.NotNull;
import lombok.*;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class NewUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column
    @NonNull
    String username;

    @Column
    @NonNull
    String details;

    @Column
    @NonNull
    String password;
}
