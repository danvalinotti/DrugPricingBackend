package com.galaxe.drugpriceapi.src.ResponseRequestObjects.WellRxRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WellRxRequest {
    String drugname;
    String lat;
    String lng;
    String numdrugs;
    String bgIndicator;
    String ncpdps;
}
