package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Table
@Entity
@Getter
@Setter
public class Report_Drugs {
    @Id
    @GeneratedValue
    int id ;
    @Column
    int reportId;
    @Column
    int priceId;
}
