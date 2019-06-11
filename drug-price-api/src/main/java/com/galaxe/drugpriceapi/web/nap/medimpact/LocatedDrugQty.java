package com.galaxe.drugpriceapi.web.nap.medimpact;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocatedDrugQty {

    private String quantity;

    private String gsn;

    private String quantityUom;

    private String isSelected;

    private String ranking;

}
