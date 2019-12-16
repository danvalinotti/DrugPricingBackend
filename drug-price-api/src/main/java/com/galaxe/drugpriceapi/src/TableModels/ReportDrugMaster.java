package com.galaxe.drugpriceapi.src.TableModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "report_dm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportDrugMaster {
    @Id
    @GeneratedValue
    int id;
    @Column
    Integer drugId;
    @Column
    String schedule;
}
