package com.galaxe.drugpriceapi.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DrugNAP2 {

    private String zip;

    private String gpi;

    private Dosage dosage;

    private String city;

    private String state;

    private String productName;

    private List<PriceList> priceList;

}
