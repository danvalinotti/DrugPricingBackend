package com.galaxe.drugpriceapi.src.TableModels;

import lombok.*;

import javax.persistence.*;
import java.util.Date;



@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Price {
    @Id
    @GeneratedValue
    int id;
    @Column
    Double price;
    @Column
    String pharmacy ;
    @Column
    Double difference;
    @Column
    int programId;
    @Column
    int drugDetailsId;
    @Column
    Double recommendedPrice;
    @Column
    Double lowestMarketPrice;
    @Column
    Double averagePrice;
    @Column
    Integer rank;
    @Column
    Double uncPrice;


    @Temporal(TemporalType.TIMESTAMP)
    Date createdat;

    @PrePersist
    public void beforeInsert(){
//
//        try{
//        }catch(Exception e ){
//
//        }

    }


}
