package com.galaxe.drugpriceapi.web.nap.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostObject {

    private String referrer;

    private String quantity;

    private String latitude;

    private String site_identity;

    private String ndc;

    private String longitude;

}
