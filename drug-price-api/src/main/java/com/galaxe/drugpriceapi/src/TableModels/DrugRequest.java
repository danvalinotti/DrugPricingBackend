package com.galaxe.drugpriceapi.src.TableModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

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
    String drugId;
    @Column
    String drugType;
    @Column
    String dosageStrength;
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
    @Column
    String good_rx_id;


}
