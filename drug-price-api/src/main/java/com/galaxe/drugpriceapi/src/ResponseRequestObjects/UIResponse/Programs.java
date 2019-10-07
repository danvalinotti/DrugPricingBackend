package com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Programs {
    List<PriceDetails> prices;
}
