package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.PriceRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.listeners.PriceListener;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.util.Date;



@Entity
//@EntityListeners(PriceListener.class)
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
