package com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pricing {

    private String macPrice;

    private String price;

    private String usualAndCustomaryPrice;

    private String awpPrice;

    private String priceBasis;

}
