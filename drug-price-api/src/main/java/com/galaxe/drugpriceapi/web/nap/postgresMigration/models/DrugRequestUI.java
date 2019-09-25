package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugMasterRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DrugRequestUI {

    @Autowired
    DrugMasterRepository drugMasterRepository;

    int requestId;
    DrugMaster drugMaster;
    String program;
    String longitude;
    String latitude;
    String ndc;
    String quantity;
    String zipcode;
    String drugName;
    String brandIndicator;
    String gsn;

    public DrugRequestUI(DrugRequest drugRequest){
        this.requestId = drugRequest.getId();
//        try {
//            this.drugMaster = drugMasterRepository.findById(drugRequest.getDrugId()).get();
//        }catch (Exception ex){
//            System.out.println("DRUG ID NOT FOUND:"+drugRequest.getDrugId());
//        }
        switch(drugRequest.getProgramId()){
            case 0:
                this.program = "InsideRx";
                break;
            case 1:
                this.program ="U.S Pharmacy Card";
                break;
            case 2:
                this.program ="WellRx";
                break;
            case 3:
                this.program ="MedImpact";
                break;
            case 4:
                this.program ="SingleCare";
                break;
            default:
                this.program ="Blink Health";
                break;
        }
//        this.program = drugRequest.getProgramId() ;
        this.longitude = drugRequest.getLongitude();
        this.latitude = drugRequest.getLatitude();
        this.ndc= drugRequest.getNdc();
        this.quantity= drugRequest.getQuantity();
        this.zipcode= drugRequest.getZipcode();
        this.drugName= drugRequest.getDrugName();
        this.brandIndicator= drugRequest.getBrandIndicator();
        this.gsn= drugRequest.getGsn();
    }

}
