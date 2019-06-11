package com.galaxe.drugpriceapi.web.nap.blinkhealth;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Dosages {

    private String med_id;

    private List<Quantities> quantities;

    private String is_custom_quantity_allowed;

    private String display_dosage;

    private String selected_quantity_index;
}
