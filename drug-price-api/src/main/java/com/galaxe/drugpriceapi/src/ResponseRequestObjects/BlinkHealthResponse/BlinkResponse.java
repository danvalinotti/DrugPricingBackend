package com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse;

import com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.PharmacyResponse.PharmacyDetails;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.PriceResponse.Price;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlinkResponse {

    PharmacyDetails pharmacyDetails;
    Price price;
}
