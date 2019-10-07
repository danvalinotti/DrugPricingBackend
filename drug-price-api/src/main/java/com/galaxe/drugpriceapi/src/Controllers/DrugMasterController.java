package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.USPharmResponse.USPharmResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.InsideRxResponse.InsideRxResponse;
import com.galaxe.drugpriceapi.src.Helpers.PricesAndMaster;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.BlinkResponse;
import com.galaxe.drugpriceapi.src.Services.*;

import com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse.LocatedDrug;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.GoodRxResponse.GoodRxResponse;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.SinglecareResponse.PharmacyPricings;

import com.galaxe.drugpriceapi.src.ResponseRequestObjects.WellRxResponse.Drugs;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


import java.util.*;
import java.util.concurrent.CompletableFuture;



@CrossOrigin
@RestController
public class DrugMasterController {

    @Autowired
    DrugMasterRepository drugMasterRepository;

    //DRUGS
    //--------------------------------------------------------------

    @GetMapping("/drugmaster/get/all")
    private List<DrugMaster> getDrugMasters() {
        return drugMasterRepository.getAllWithoutZipCode();
    }

    @GetMapping("/drugmaster/get/id/{id}")
    private DrugMaster getById(@PathVariable String id) {
        Integer newId = Integer.parseInt(id);
        return drugMasterRepository.findById(newId).get();
    }




}
