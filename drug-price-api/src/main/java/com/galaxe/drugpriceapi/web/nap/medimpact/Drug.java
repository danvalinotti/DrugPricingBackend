package com.galaxe.drugpriceapi.web.nap.medimpact;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Drug {

    private String brandGenericIndicator;

    private String gsn;

    private String quantity;

    private String drugRanking;

    private String labelName;

    private String quantityRanking;

    private String ndcCode;
}
