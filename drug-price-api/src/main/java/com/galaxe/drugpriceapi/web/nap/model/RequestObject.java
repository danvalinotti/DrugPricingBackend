package com.galaxe.drugpriceapi.web.nap.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestObject {

    private String zipcode;

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

}

