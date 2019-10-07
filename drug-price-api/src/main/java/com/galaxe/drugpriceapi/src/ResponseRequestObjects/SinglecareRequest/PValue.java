package com.galaxe.drugpriceapi.src.ResponseRequestObjects.SinglecareRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PValue {

    private String NDC;

    private String Quantity;

    private String ZipCode;

    private int Distance;

    private int MaxResults;

    private int TenantId;

    private String NABP;


}
