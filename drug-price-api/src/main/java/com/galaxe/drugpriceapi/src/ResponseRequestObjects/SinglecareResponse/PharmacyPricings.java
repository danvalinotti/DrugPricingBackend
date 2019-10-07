package com.galaxe.drugpriceapi.src.ResponseRequestObjects.SinglecareResponse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PharmacyPricings {

    private Pharmacy Pharmacy;

    private List<Prices> Prices;


}
