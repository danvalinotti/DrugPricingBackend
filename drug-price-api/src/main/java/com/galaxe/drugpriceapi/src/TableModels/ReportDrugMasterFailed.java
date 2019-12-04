package com.galaxe.drugpriceapi.src.TableModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "report_dm_failed")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportDrugMasterFailed {
    @Id
    @GeneratedValue
    int id;
    @Column
    String name;
    @Column
    String dosageStrength;
    @Column
    String ndc;
    @Column
    Double quantity;
    @Column
    String zipCode;
    @Column
    String drugType;
    @Column
    String dosageUOM;
    @Column
    Boolean reportFlag;
    @Column
    String gsn;
}
