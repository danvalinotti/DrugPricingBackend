package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DrugSchedule {

    @Id
    @GeneratedValue
    int id;
    @Column
    int drugMasterId;
    @Column
    Date reportTime;
    @Column
    boolean isActive;

}
