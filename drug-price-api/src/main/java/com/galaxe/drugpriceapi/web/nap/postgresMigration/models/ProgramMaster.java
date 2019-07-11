package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramMaster {
    @Id
    @GeneratedValue
    int id;
    @Column
    String name;
    @Column
    boolean isactive;
}
