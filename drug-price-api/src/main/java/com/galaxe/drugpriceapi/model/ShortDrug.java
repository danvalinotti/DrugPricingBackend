package com.galaxe.drugpriceapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShortDrug {
    String name;
    String dosageStrength;
    String dosageUOM;
    String quantity;
    String drugType;
    String zipCode;
}
