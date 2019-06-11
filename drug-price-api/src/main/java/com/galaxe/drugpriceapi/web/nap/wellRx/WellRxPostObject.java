package com.galaxe.drugpriceapi.web.nap.wellRx;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WellRxPostObject {

    String drugname;

    String lat;

    String lng;

    String numdrugs;

    String qty;

    String ncpdps;
}
