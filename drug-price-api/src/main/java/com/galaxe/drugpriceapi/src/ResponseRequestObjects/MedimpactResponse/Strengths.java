package com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Strengths {

    private List<LocatedDrugStrength> locatedDrugStrength;
}
