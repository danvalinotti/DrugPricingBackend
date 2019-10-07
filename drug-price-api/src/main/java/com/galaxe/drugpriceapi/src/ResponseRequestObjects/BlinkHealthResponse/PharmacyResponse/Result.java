package com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.PharmacyResponse;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class Result {

    private List<PharmacyDetails> results;
}
