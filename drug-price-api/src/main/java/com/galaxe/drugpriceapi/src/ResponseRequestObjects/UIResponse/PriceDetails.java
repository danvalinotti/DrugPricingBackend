package com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceDetails {

    String program;

    String pharmacy;

    String price;

    String uncPrice;

    Boolean uncPriceFlag;

    String diff;

    String diffPerc;

}
