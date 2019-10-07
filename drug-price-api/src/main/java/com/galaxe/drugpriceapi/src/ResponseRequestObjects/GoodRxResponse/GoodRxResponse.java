package com.galaxe.drugpriceapi.src.ResponseRequestObjects.GoodRxResponse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoodRxResponse {
    List<GoodRxResult> results;
}
