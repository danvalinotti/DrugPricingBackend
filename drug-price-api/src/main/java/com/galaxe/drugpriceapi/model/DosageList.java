package com.galaxe.drugpriceapi.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DosageList {

    private String gpi;

    private String gpiCode;

    private String dosageUOM;

    private DosageList dosageForm;

    private String description;

    private String dosageStrength;

    private String ndc;
}