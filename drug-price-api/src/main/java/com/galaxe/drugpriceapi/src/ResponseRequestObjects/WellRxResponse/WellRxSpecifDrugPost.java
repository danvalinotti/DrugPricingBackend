package com.galaxe.drugpriceapi.src.ResponseRequestObjects.WellRxResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WellRxSpecifDrugPost {

    private String bgIndicator;

    private String ncpdps;

    private String GSN;

    private String quantity;

    private String bReference;

    private String lng;

    private String numdrugs;

    private String lat;
}
