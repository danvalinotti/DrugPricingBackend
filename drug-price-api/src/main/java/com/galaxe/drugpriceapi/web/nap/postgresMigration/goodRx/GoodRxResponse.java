package com.galaxe.drugpriceapi.web.nap.postgresMigration.goodRx;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoodRxResponse {
    List<GoodRxResult> results;
}
