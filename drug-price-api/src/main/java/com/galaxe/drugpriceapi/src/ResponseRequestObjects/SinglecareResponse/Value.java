package com.galaxe.drugpriceapi.src.ResponseRequestObjects.SinglecareResponse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Value {

    private List<PharmacyPricings> PharmacyPricings;
    private String NDC;
}
