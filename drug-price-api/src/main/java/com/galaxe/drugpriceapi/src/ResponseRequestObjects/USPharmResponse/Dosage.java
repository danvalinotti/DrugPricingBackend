package com.galaxe.drugpriceapi.src.ResponseRequestObjects.USPharmResponse;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Dosage {


    private String gpi;

    private String gpiCode;

    public String dosageUOM;

    private DosageForm dosageForm;

    private String description;

    private String dosageStrength;

    private String ndc;


}
