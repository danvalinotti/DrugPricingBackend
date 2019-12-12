package com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse;

import java.util.Comparator;

public class PriceDetailsComparator implements Comparator<PriceDetails> {
    @Override
    public int compare(PriceDetails priceDetails1, PriceDetails priceDetails2) {
        return priceDetails1.getRank().compareTo(priceDetails2.getRank());
    }
}
