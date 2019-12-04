package com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UIRequestObject {
    private String id;

    private String zipcode;

    private String ndc;

    private String drugName;

    private String dosageStrength;

    private Double quantity;

    private String drugNDC;

    private String drugType;

    private String longitude;

    private String latitude;

    private String program;

    private String token;

    private Boolean reportFlag;

    private String GSN;

    private String brandIndicator;

}

