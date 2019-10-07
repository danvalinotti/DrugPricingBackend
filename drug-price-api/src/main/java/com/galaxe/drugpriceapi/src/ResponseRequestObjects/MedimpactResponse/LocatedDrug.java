package com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocatedDrug {

    private Pharmacy pharmacy;

    private Pricing pricing;

}
