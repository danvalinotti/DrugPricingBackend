package com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocatedDrugStrength {

    private String gsn;

    private String strength;

    private String isSelected;

    private String ranking;

}
