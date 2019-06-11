package com.galaxe.drugpriceapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

@Getter
@Setter
public class Prices {

    private String npi;

    private String price;

    private InsideRxPharmacy pharmacy;

    private String type;

    private String uncPrice;

    private String key;


}
