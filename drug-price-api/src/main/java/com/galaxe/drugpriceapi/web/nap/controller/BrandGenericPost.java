package com.galaxe.drugpriceapi.web.nap.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandGenericPost {
    String drugname;
    String lat;
    String lng;
    String numdrugs;
    String bgIndicator;
    String ncpdps;
}
