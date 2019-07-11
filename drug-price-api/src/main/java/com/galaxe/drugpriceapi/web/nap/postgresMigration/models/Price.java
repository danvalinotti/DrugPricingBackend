package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

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

}
