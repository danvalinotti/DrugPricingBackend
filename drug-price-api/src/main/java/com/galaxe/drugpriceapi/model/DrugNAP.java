package com.galaxe.drugpriceapi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;

@Getter
@Setter
@Document(collection = "drugNAP")
public class DrugNAP {
    @Id
    private String id;
    private String drugId;
    private String nationalAverage;
    @Version
    private String version;
    private String ndc;
    private String name;
    private String pharmacyType;
    private String pricingUnit;
    private String asOfDate;
    private String effectiveDate;
    @CreatedDate
    private ZonedDateTime createdAt;
    @LastModifiedDate
    private ZonedDateTime lastModified;
}
