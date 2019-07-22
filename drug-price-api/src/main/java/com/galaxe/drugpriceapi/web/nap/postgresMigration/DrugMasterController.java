package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxe.drugpriceapi.model.*;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient2;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient3;
import com.galaxe.drugpriceapi.web.nap.controller.PriceController;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterList;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterListTestController;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrug;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@CrossOrigin
@RestController
public class DrugMasterController {

    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    PriceController priceController;
    @Autowired
    APIClient apiService;

    @Autowired
    APIClient2 apiService2;

    @Autowired
    APIClient3 apiService3;



    //DRUGS
    //--------------------------------------------------------------
    private DrugMaster saveDrugMaster(DrugMaster drugMaster) {
        return drugMasterRepository.save(drugMaster);
    }

    @GetMapping("/drugmaster/get/all")
    private List<DrugMaster> getDrugMasters() {
        return drugMasterRepository.findAll();
    }


    public PricesAndMaster getDetails(RequestObject requestObject, DrugMaster drugMaster) throws Throwable {

        long start = System.currentTimeMillis();
        Map<String, String> longitudeLatitude = priceController.constructLongLat(requestObject.getZipcode());
        System.out.println("LATLONG:" + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        String brandType = priceController.getBrandIndicator(requestObject).intern();
        System.out.println("GetBrandInd:" + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        if (brandType.isEmpty()) {
            brandType = "B";
            requestObject.setDrugType("BRAND_WITH_GENERIC");
        } else {
            requestObject.setDrugType(brandType.equalsIgnoreCase("G") ? "GENERIC" : "BRAND_WITH_GENERIC");
        }

        start = System.currentTimeMillis();
        CompletableFuture<Blink> blinkFuture = null;
        //Future result
        CompletableFuture<List<InsideRx>> inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);
        CompletableFuture<List<DrugNAP2>> usPharmacy = apiService2.constructUsPharmacy(requestObject);
        CompletableFuture<List<Drugs>> wellRxFuture = apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
        CompletableFuture<LocatedDrug> medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
        CompletableFuture<PharmacyPricings> singleCareFuture = apiService.getSinglecarePrices(requestObject);

        blinkFuture = apiService3.getBlinkPharmacyPrice(requestObject);


        //Wait until they are all done
        if (blinkFuture != null)
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture, blinkFuture).join();
        else {
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture).join();
            //    System.out.println("AllProviders:"+(System.currentTimeMillis()-start));
            //   start = System.currentTimeMillis();
        }


        // System.out.println("After all API call done : " + (System.currentTimeMillis() - start));
        //List and obj to store future result
        List<InsideRx> insideRxPrices = inside.get();
        List<DrugNAP2> usPharmacyPrices = usPharmacy.get();
        List<Drugs> wellRx = wellRxFuture.get();
        LocatedDrug locatedDrug = medImpactFuture.get();
        PharmacyPricings singleCarePrice = singleCareFuture.get();
        Blink blink = null;
        if (blinkFuture != null)
            blink = apiService3.getBlinkPharmacyPrice(requestObject).get();

        PricesAndMaster pricesAndMaster = new PricesAndMaster();
        List<Price> prices = new ArrayList<>();

        InsideRx insideRx = insideRxPrices.get(0);
        Price p = new Price();
        try {
            p.setPrice(Double.parseDouble(insideRx.getPrices().get(0).getPrice()));
            p.setPharmacy(insideRx.getPrices().get(0).getPharmacy().getName());
            p.setDrugDetailsId(drugMaster.getId());
            p.setProgramId(0);
        } catch (Exception e) {
            p = null;
        }
        DrugNAP2 usPharm = usPharmacyPrices.get(0);
        Price p1 = new Price();
        try {
            p1.setPrice(Double.parseDouble(usPharm.getPriceList().get(0).getDiscountPrice()));
            p1.setPharmacy(usPharm.getPriceList().get(0).getPharmacy().getPharmacyName());
            p1.setProgramId(1);
            p1.setDrugDetailsId(drugMaster.getId());
        } catch (Exception e) {
            p1 = null;
        }

        Price p2 = new Price();
        try {
            Drugs well = wellRx.get(0);
            p2.setPrice(Double.parseDouble(well.getPrice()));
            p2.setPharmacy(well.getPharmacyName());
            p2.setProgramId(2);
            p2.setDrugDetailsId(drugMaster.getId());
        } catch (Exception e) {
            p2 = null;
        }
        //MediIMpact
        Price p3 = new Price();
        try {
            p3.setPrice(Double.parseDouble(locatedDrug.getPricing().getPrice()));
            p3.setPharmacy(locatedDrug.getPharmacy().getName());
            p3.setProgramId(3);
            p3.setDrugDetailsId(drugMaster.getId());
        } catch (Exception e) {
            p3 = null;
        }
        Price p4 = new Price();
        try {
            p4.setPrice(Double.parseDouble(singleCarePrice.getPrices().get(0).getPrice()));
            p4.setPharmacy(singleCarePrice.getPharmacy().getName());
            p4.setProgramId(4);
            p4.setDrugDetailsId(drugMaster.getId());
        } catch (Exception e) {
            p4 = null;
        }
        Price p5 = new Price();
        try {
            p5.setPrice(Double.parseDouble(blink.getPrice().getLocal().getRaw_value()));
            p5.setPharmacy(blink.getResults().getName());
            p5.setProgramId(5);
            p5.setDrugDetailsId(drugMaster.getId());
        } catch (Exception e) {
            p5 = null;
        }
        prices.add(p);
        prices.add(p1);
        prices.add(p2);
        prices.add(p3);
        prices.add(p4);
        prices.add(p5);


        pricesAndMaster.setDrugMaster(drugMaster);
        pricesAndMaster.setPrices(prices);

        return pricesAndMaster;
//        start = System.currentTimeMillis();
//        MongoEntity entity =  priceController.constructEntity(usPharmacyPrices, insideRxPrices, requestObject, wellRx, locatedDrug, singleCarePrice, blink);
//        MongoEntity newEntity = new MongoEntity();
//
//        System.out.println("ConstructEntity:"+(System.currentTimeMillis()-start));
//        start = System.currentTimeMillis();
//        MongoEntity m  =  priceController.updateDiff(entity,requestObject);
//        System.out.println("updateDiff:"+(System.currentTimeMillis()-start));


    }




}
