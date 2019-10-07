package com.galaxe.drugpriceapi.src.ResponseRequestObjects.GoodRxResponse;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodRxPrice {
    Double price;
    String display_noun;//If it requires membership or not
}
