package com.galaxe.drugpriceapi.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrugInfo {


    private String gpi10;

    private String brandChanged;

    private Dosage dosage;

    private DosageList[] dosageList;

    private QuantityList quantity;

    private String bin;

    private DosageList dosageForm;

    private QuantityList[] quantityList;

    private BrandList[] brandList;

    private String productName;

    private String dosageFormChanged;

    private BrandList brand;

    private DosageFormList[] dosageFormList;
}
