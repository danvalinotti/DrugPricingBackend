package com.galaxe.drugpriceapi.web.nap.blinkhealth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Results {

    private String name;

    private Location location;

    private Brand brand;

    private String is_supersaver;

}
