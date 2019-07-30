package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

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
public class AlertType {
    @Id
    @GeneratedValue
    int id;
    @Column
    String name;
    @Column
    String header;
    @Column
    String footer;
    @Column
    String summary;
    @Column
    String deliveryType;
    @Column
    Boolean active ;
    @Column
    String recipients;



}
