package com.galaxe.drugpriceapi.src.ResponseRequestObjects.InsideRxResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsideRxPrice {

    private String npi;

    private String price;

    private InsideRxPharmacy pharmacy;

    private String type;

    private String uncPrice;

    private String key;


}
