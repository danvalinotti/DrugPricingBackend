package com.galaxe.drugpriceapi.src.ResponseRequestObjects.USPharmResponse;


import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class USPharmResponse {

    private String zip;

    private String gpi;

    private Dosage dosage;

    private String city;

    private String state;

    private String productName;

    private List<USPharmPrice> priceList;

    private String dosageUOM;

}


