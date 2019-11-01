package com.galaxe.drugpriceapi.src.TableModels;

import lombok.*;

import javax.persistence.*;

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
