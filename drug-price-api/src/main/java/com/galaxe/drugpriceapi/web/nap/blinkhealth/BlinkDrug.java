package com.galaxe.drugpriceapi.web.nap.blinkhealth;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BlinkDrug {

    private String selected_quantity_pinned;

    private String selected_dosage_index;

    private String med_type;

    private String med_name_id;

    private String selected_form_index;

    private String is_telemed_eligible;

    private String formatted_name;

    private List<Forms> forms;

    private String slug;

    private String selected_quantity_index;

    private String formatted_alternate_name;

}
