package com.galaxe.drugpriceapi.model;


import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

@Getter
@Setter
public class PriceList {


    private String discountPrice;

    private Pharmacy pharmacy;

}