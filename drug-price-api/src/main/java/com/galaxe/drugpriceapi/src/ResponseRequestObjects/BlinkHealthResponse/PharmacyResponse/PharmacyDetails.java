package com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.PharmacyResponse;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PharmacyDetails {

    private String name;

    private Location location;

    private Brand brand;

    private String is_supersaver;

}
