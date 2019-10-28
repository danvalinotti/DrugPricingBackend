package com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PriceLeadingDrug {

    @Id
    private int id;

    private String name;

    private String dosageStrength;

    private double quantity;

    private String ndc;

    private double recommendedPrice;

    private double price;

    private double averagePrice;

    private int drugDetailsId;

    private String pharmacy;

    private int programId;

    private int rank;

    private String zipCode;

    private int reportId;

    private int priceId;

    private int dmId;
}
