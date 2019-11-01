package com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeadingResponseObject {

    private String name;

    private String dosageStrength;

    private double quantity;

    private String ndc;

    private double recommendedPrice;

    private double price;

    private double averagePrice;

    private int drugDetailsId;

    private List<PriceDetails> programs;

    private String zipCode;

    private Double diff;

    private String nextLeadingProgram;

}
