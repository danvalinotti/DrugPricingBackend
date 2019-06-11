package com.galaxe.drugpriceapi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
public class DrugBrand {

    private String gpi10;

    private String brandName;

    private String drugType;

    private String productName;
}

