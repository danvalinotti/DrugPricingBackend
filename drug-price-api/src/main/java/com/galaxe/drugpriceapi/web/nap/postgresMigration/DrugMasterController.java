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
import com.galaxe.drugpriceapi.web.nap.postgresMigration.goodRx.GoodRxResponse;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
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

    @Autowired
    DrugRequestRepository drugRequestRepository;


    //DRUGS
    //--------------------------------------------------------------
    private DrugMaster saveDrugMaster(DrugMaster drugMaster) {
        return drugMasterRepository.save(drugMaster);
    }

    @GetMapping("/drugmaster/get/all")
    private List<DrugMaster> getDrugMasters() {
        return drugMasterRepository.getAllWithoutZipCode();
    }
    @GetMapping("/drugmaster/get/id/{id}")
    private DrugMaster getById(@PathVariable String id) {
        Integer newId = Integer.parseInt(id);
        return drugMasterRepository.findById(newId).get();
    }
    @GetMapping("/drugmaster/update/gsn")
    private List<DrugMaster> updateGSNs() {
        List<DrugMaster > drugMasters = drugMasterRepository.findAll();
        for (DrugMaster drugMaster:drugMasters ) {
            String gsn ="";
            try {
                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugMaster.getId()+"", 2).get(0);
                gsn = drugRequest.getGsn();
                drugMaster.setGsn(gsn);
                drugMasterRepository.save(drugMaster);
            }catch (Exception ex){

            }
        }
        return drugMasters;
    }
    @GetMapping("/remove/duplicates")
    public void removeDuplicates() {
      List<DrugMaster> allDrugs = drugMasterRepository.findAll();
        for (DrugMaster drug: allDrugs) {
            if(drugMasterRepository.findAllByFields(drug.getNdc(),drug.getQuantity(),drug.getZipCode()).size()>1){
                drugMasterRepository.delete(drug);
            }

        }
    }
    @GetMapping("/set/all/true")
    public void drugsAllTrue() {
        drugMasterRepository.setAllTrue();
    }
    @GetMapping("/reset/dosageStrengths")
    public void resetDosageStrengths() {
        Gson gson= new Gson();
        int dosageIndex = 0;
        for (DrugMaster drug: drugMasterRepository.findAll()) {
//            System.out.println(priceController.getDrugInfo(drug.getName()));
            Type type = new TypeToken<List<DrugDosage>>() {}.getType();
            String priceStr = priceController.getDrugInfo(drug.getName());
            List<DrugDosage> drugInfo= gson.fromJson(priceStr, type);
            int i = 0;
            for (Dose dose: drugInfo.get(0).getDose()) {

                if(dose.getValue().equalsIgnoreCase(drug.getNdc())){
                    System.out.println(i);
                    dosageIndex = i;
                }
                i++;
            }
            i=0;
           // drugInfo.get(0).getDose().get(dosageIndex).getDosage();
            drug.setDosageStrength(drugInfo.get(0).getDose().get(dosageIndex).getLabel());
           drugMasterRepository.save(drug);
            // drug.setDosageUOM();
        }
    }



    public PricesAndMaster getDetails(RequestObject requestObject, DrugMaster drugMaster) throws Throwable {

        long start = System.currentTimeMillis();
        Map<String, String> longitudeLatitude = new HashMap<>();
        longitudeLatitude.put("longitude",requestObject.getLongitude());
        longitudeLatitude.put("latitude", requestObject.getLatitude());

        start = System.currentTimeMillis();
        String brandType = priceController.getBrandIndicator(requestObject).intern();

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


            CompletableFuture<List<InsideRx>> inside = null;
            try {
                inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);
            }catch (Exception ex){
                inside = CompletableFuture.completedFuture(new ArrayList<>());
            }
            CompletableFuture<List<DrugNAP2>> usPharmacy = null;
            try {
                usPharmacy = apiService2.constructUsPharmacy(requestObject);
            }catch (Exception ex){
            usPharmacy = CompletableFuture.completedFuture(new ArrayList<>());
            }

            CompletableFuture<List<Drugs>> wellRxFuture  = null;
            try {
                wellRxFuture =   apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
            }catch (Exception ex){
                wellRxFuture = CompletableFuture.completedFuture(new ArrayList<>());
            }
            CompletableFuture<LocatedDrug> medImpactFuture  = null;
            try {
                medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
            }catch (Exception ex){
                medImpactFuture = CompletableFuture.completedFuture(new LocatedDrug());
            }
            CompletableFuture<PharmacyPricings> singleCareFuture  = null;
            try {
                singleCareFuture =   apiService.getSinglecarePrices(requestObject);
            }catch (Exception ex){
                singleCareFuture = CompletableFuture.completedFuture(new PharmacyPricings());
            }

        CompletableFuture<GoodRxResponse> goodRxFuture  = null;
        try {
            goodRxFuture =   apiService.getGoodRxPrices(requestObject);
        }catch (Exception ex){
            goodRxFuture = CompletableFuture.completedFuture(new GoodRxResponse());
        }
        try {
            blinkFuture = apiService3.getBlinkPharmacyPrice(requestObject);
        }catch (Exception ex){
            blinkFuture = CompletableFuture.completedFuture(new Blink());
        }

            //Wait until they are all done
            if (blinkFuture != null)
                CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture,goodRxFuture, blinkFuture).join();
            else {
                CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture,goodRxFuture).join();
                //   start = System.currentTimeMillis();
            }


        //List and obj to store future result
        List<InsideRx> insideRxPrices = inside.get();
        List<DrugNAP2> usPharmacyPrices = usPharmacy.get();
        List<Drugs> wellRx = wellRxFuture.get();
        LocatedDrug locatedDrug = medImpactFuture.get();
        PharmacyPricings singleCarePrice = singleCareFuture.get();

        GoodRxResponse goodRxPrice;
        try {
            if(requestObject.getDrugName().toUpperCase().equals("ATORVASTATIN CALCIUM")) {
                System.out.println("ACYCLOVIR");
            }
            if(requestObject.getDrugName().toUpperCase().equals("CIPROFLOXACIN HCL")) {
                System.out.println("ACYCLOVIR");
            }
            if(requestObject.getDrugName().toUpperCase().equals("CIPROFLOXACIN HYDROCHLORIDE")) {
                System.out.println("ACYCLOVIR");
            }
            if(requestObject.getDrugName().toUpperCase().equals("CITALOPRAM HYDROBROMIDE")) {
                System.out.println("ACYCLOVIR");
            }
            if(requestObject.getDrugName().toUpperCase().equals("CLOMIPHENE CITRATE")) {
                System.out.println("ACYCLOVIR");
            }
            if(requestObject.getDrugName().toUpperCase().equals("ERGOCALCIFEROL")) {
                System.out.println("ACYCLOVIR");
            }
              goodRxPrice  =goodRxFuture.get();

        }catch (Exception ex){
            goodRxPrice = new GoodRxResponse();
        }
        Blink blink = null;
        if (blinkFuture != null)
            blink = apiService3.getBlinkPharmacyPrice(requestObject).get();

        PricesAndMaster pricesAndMaster = new PricesAndMaster();
        List<Price> prices = new ArrayList<>();


        Price p = new Price();
        try {
            InsideRx insideRx = insideRxPrices.get(0);
            p.setPrice(Double.parseDouble(insideRx.getPrices().get(0).getPrice()));
            p.setPharmacy(insideRx.getPrices().get(0).getPharmacy().getName());
            p.setDrugDetailsId(drugMaster.getId());
            p.setProgramId(0);
            p.setCreatedat(new Date());
        } catch (Exception e) {
            p = null;
        }

        Price p1 = new Price();
        try {
            DrugNAP2 usPharm = usPharmacyPrices.get(0);
            p1.setPrice(Double.parseDouble(usPharm.getPriceList().get(0).getDiscountPrice()));
            p1.setPharmacy(usPharm.getPriceList().get(0).getPharmacy().getPharmacyName());
            p1.setProgramId(1);
            p1.setDrugDetailsId(drugMaster.getId());
            p1.setCreatedat(new Date());
        } catch (Exception e) {
            p1 = null;
        }

        Price p2 = new Price();
        try {
            Drugs well = wellRx.get(0);

            if(requestObject.getDrugName().equalsIgnoreCase("Genotropin") && requestObject.getDosageStrength().contains("1.6")){System.out.println("GOT Drug");}
            p2.setPrice(Double.parseDouble(well.getPrice()));
            if(requestObject.getDrugName().equalsIgnoreCase("Genotropin") && requestObject.getDosageStrength().contains("1.6")){System.out.println("GOT price");}
            p2.setPharmacy(well.getPharmacyName());
            if(requestObject.getDrugName().equalsIgnoreCase("Genotropin") && requestObject.getDosageStrength().contains("1.6")){System.out.println("GOT pharmacy");}
            p2.setProgramId(2);
            p2.setCreatedat(new Date());
            if(requestObject.getDrugName().equalsIgnoreCase("Genotropin") && requestObject.getDosageStrength().contains("1.6")){System.out.println("GOT program");}
            p2.setDrugDetailsId(drugMaster.getId());
            if(requestObject.getDrugName().equalsIgnoreCase("Genotropin") && requestObject.getDosageStrength().contains("1.6")){System.out.println("GOT id");}
        } catch (Exception e) {
            if(requestObject.getDrugName().equalsIgnoreCase("Genotropin") && requestObject.getDosageStrength().contains("1.6")){System.out.println("Return null"+ prices.size());}

            p2 = null;
        }
        //MediIMpact
        Price p3 = new Price();
        try {
            p3.setPrice(Double.parseDouble(locatedDrug.getPricing().getPrice()));
            p3.setPharmacy(locatedDrug.getPharmacy().getName());
            p3.setProgramId(3);
            p3.setDrugDetailsId(drugMaster.getId());
            p3.setCreatedat(new Date());
        } catch (Exception e) {
            p3 = null;
        }
        Price p4 = new Price();
        try {
            p4.setPrice(Double.parseDouble(singleCarePrice.getPrices().get(0).getPrice()));
            p4.setPharmacy(singleCarePrice.getPharmacy().getName());
            p4.setProgramId(4);
            p4.setDrugDetailsId(drugMaster.getId());
            p4.setCreatedat(new Date());
        } catch (Exception e) {
            p4 = null;
        }
        Price p5 = new Price();
        try {
            p5.setPrice(Double.parseDouble(blink.getPrice().getLocal().getRaw_value()));

            p5.setPharmacy(blink.getResults().getName());
            p5.setProgramId(5);
            p5.setDrugDetailsId(drugMaster.getId());
            p5.setCreatedat(new Date());
        } catch (Exception e) {
            p5 = null;
        }
        Price p6 = new Price();
        try {

            try {
                p6.setPrice(goodRxPrice.getResults().get(0).getPrices().get(0).getPrice());
            }catch (Exception ex){

            }
            p6.setPharmacy(goodRxPrice.getResults().get(0).getPharmacy().getName());
            p6.setProgramId(6);
            p6.setDrugDetailsId(drugMaster.getId());
            p6.setCreatedat(new Date());
        } catch (Exception e) {
            p6 = null;
        }
        prices.add(p);
        prices.add(p1);
        prices.add(p2);
        prices.add(p3);
        prices.add(p4);
        prices.add(p5);
        prices.add(p6);
        if(requestObject.getDrugName().equalsIgnoreCase("Genotropin") && requestObject.getDosageStrength().contains("1.6")){System.out.println("B4 Prices size"+ prices.size());}

        pricesAndMaster.setDrugMaster(drugMaster);

        pricesAndMaster.setPrices(prices);

        return pricesAndMaster;
//        start = System.currentTimeMillis();
//        MongoEntity entity =  priceController.constructEntity(usPharmacyPrices, insideRxPrices, requestObject, wellRx, locatedDrug, singleCarePrice, blink);
//        MongoEntity newEntity = new MongoEntity();
//
//        start = System.currentTimeMillis();
//        MongoEntity m  =  priceController.updateDiff(entity,requestObject);


    }




}
