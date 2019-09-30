package com.galaxe.drugpriceapi.web.nap.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Program {

    String program;

    String pharmacy;

    String price;

    Boolean uncPriceFlag;

    String diff;

    String diffPerc;

}
