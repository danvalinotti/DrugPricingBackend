package com.galaxe.drugpriceapi.web.nap.postgresMigration.goodRx;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodRxPrice {
    Double price;
    String display_noun;//If it requires membership or not
}
