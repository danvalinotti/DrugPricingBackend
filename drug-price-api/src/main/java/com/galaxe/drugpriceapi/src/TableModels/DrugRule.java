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
public class DrugRule {
    @Id
    @GeneratedValue
    int id;
    @Column
    int drugId;
    @Column
    Double percentChange;
    @Column
    int  alertTypeId;

}
