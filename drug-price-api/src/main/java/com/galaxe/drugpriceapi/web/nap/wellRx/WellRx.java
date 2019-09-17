package com.galaxe.drugpriceapi.web.nap.wellRx;

import com.galaxe.drugpriceapi.web.nap.blinkhealth.Form;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WellRx {

    private List<Drugs> Drugs;

    private List<Strengths> Strengths;

    private List<Form> Forms;

}
