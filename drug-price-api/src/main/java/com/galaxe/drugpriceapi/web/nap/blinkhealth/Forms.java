package com.galaxe.drugpriceapi.web.nap.blinkhealth;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Forms {

    private String display_form;

    private String selected_dosage_index;

    private List<Dosages> dosages;
}
