package com.galaxe.drugpriceapi.src.ResponseRequestObjects.USPharmResponse;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class USPharmPrice {

    private String discountPrice;

    private Pharmacy pharmacy;

}