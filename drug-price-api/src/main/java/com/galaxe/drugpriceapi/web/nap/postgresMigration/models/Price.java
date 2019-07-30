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
    double price;
    @Column
    String pharmacy ;
    @Column
    double difference;
    @Column
    int programId;
    @Column
    int drugDetailsId;
    @Column
    double recommendedPrice;
    @Column
    double lowestMarketPrice;
    @Column
    double averagePrice;

    @Temporal(TemporalType.TIMESTAMP)
    Date createdat;

    @PrePersist
    public void beforeInsert(){
//        System.out.println("Before Insert");
//
//        try{
//            System.out.println("this"+this.getPrice());
//        }catch(Exception e ){
//
//        }

    }

}
