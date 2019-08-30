package com.galaxe.drugpriceapi.web.nap.blinkhealth;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Price {


    private Local delivery;
    private Local edlp;
    private Local local;
    private Local retail;
    private String medId;

}
