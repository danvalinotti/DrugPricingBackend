package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class  DrugMaster {
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
    double quantity;
    @Column
    String zipCode;
    @Column
    String drugType;
    @Column
    String dosageUOM;
    @Column
    boolean reportFlag;
    @Column
    String gsn;

//    public void setReportFlag(Boolean reportFlag){
//        if(reportFlag== null){
//            this.reportFlag = true;
//        }else{
//            this.reportFlag = reportFlag;
//        }
//    }





}
