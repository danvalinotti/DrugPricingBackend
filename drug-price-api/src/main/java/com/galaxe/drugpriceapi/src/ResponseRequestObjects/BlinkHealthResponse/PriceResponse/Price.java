package com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.PriceResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Price {


    private Local delivery;
    private Local edlp;
    private Local local;
    private Local retail;
    private String medId;

}
