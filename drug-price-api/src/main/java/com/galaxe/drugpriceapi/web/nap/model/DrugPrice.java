package com.galaxe.drugpriceapi.web.nap.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrugPrice {

    private String as_of_date;
    private String effective_date;
    private String ndc;
    private String ndc_description;
    private String pharmacy_type_indicator;
    private String pricing_unit;
    private String nadac_per_unit;

}
