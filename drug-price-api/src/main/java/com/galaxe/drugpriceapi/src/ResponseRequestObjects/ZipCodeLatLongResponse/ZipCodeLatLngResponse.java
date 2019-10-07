package com.galaxe.drugpriceapi.src.ResponseRequestObjects.ZipCodeLatLongResponse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ZipCodeLatLngResponse {
    private List<ZipLatLng> output;
    private String status;
}


