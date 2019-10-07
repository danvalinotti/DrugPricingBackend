package com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pharmacy {

    private String zipCode;

    private String streetAddress;

    private String city;

    private String phone;

    private String npi;

    private String latitude;

    private String name;

    private String state;

    private String longitude;
}
