package com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.PriceResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Quantities {

    private String is_custom_quantity;

    private String display_quantity;

    private String formatted_description;

    private Price price;

    private String raw_quantity;

}
