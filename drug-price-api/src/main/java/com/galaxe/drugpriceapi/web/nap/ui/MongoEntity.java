package com.galaxe.drugpriceapi.web.nap.ui;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Document(collection = "final_dashboard")
public class MongoEntity {

    @Id
    private String id;

    private String name;

    private String dosageStrength;

    private String dosageUOM;

    private String quantity;

    private String drugType;

    private String zipcode;

    private String ndc;

    private List<Programs> programs;

    private Set<String> pharmacyName;

    private String recommendedPrice;

    private String average;

    private String averageDiff;

    private String recommendedDiff;

    private String description;

    @CreatedDate
    private DateTime created;

    @LastModifiedDate
    private DateTime modified;

    private String diffFromLast;

    public String getCompositeId(String ndc, String dosage, String quantity, String zipcode) {
        return ndc + dosage + quantity + zipcode;
    }

}
