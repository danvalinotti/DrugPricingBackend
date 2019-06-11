package com.galaxe.drugpriceapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Drug {

    private String id;

    private String ndc;

    private String name;

    private String type;
}
