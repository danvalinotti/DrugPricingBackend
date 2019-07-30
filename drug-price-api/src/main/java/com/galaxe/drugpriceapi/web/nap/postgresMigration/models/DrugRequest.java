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
public class DrugRequest {
    @Id
    @GeneratedValue
    int id;
    @Column
    int drugId;
    @Column
    int programId;
    @Column
    String longitude;
    @Column
    String latitude;
    @Column
    String ndc;
    @Column
    String quantity;
    @Column
    String zipcode;
    @Column
    String drugName;
    @Column
    String brandIndicator;
    @Column
    String gsn;

}
