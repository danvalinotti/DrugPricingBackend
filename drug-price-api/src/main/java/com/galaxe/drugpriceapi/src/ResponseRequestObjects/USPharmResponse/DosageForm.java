package com.galaxe.drugpriceapi.src.ResponseRequestObjects.USPharmResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DosageForm {

    private String gpi;

    private String gpiCode;

    private String dosageUOM;

    private DosageForm dosageForm;

    private String description;

    private String dosageStrength;

    private String ndc;
}


