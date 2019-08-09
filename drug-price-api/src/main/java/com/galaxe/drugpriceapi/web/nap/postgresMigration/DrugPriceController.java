package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxe.drugpriceapi.model.DrugNAP2;
import com.galaxe.drugpriceapi.model.InsideRx;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient2;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient3;
import com.galaxe.drugpriceapi.web.nap.controller.PriceController;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterListTestController;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrug;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
public class DrugPriceController {
    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    PriceRepository priceRepository;
    @Autowired
    ReportDrugsRepository reportDrugsRepository;
    @Autowired
    PriceController priceController;
    @Autowired
    DrugMasterController drugMasterController;

    @Autowired
    APIClient apiService;

    @Autowired
    APIClient2 apiService2;

    @Autowired
    APIClient3 apiService3;

    public List<Price> updatePrices(List<Price> oldReportPrices) {
        List<Price> newPrices = new ArrayList<>();
        for (Price p : oldReportPrices) {
            try {
                newPrices.add(updatePrice(p));
            } catch (Exception e) {

            }

        }

        //  DrugMaster drugMaster = drugMasterRepository.findById(price.getId()).get();
        return newPrices;
    }

    public Price updatePrice(Price price) throws ExecutionException, InterruptedException {

        DrugMaster drugMaster = drugMasterRepository.findById(price.getDrugDetailsId()).get();
        Price newPrice = new Price();
        newPrice.setCreatedat(new Date());
        newPrice.setProgramId(price.getProgramId());
        newPrice.setDrugDetailsId(price.getDrugDetailsId());

        RequestObject requestObject = new RequestObject();
        requestObject.setQuantity(drugMaster.getQuantity());
        requestObject.setDrugType(drugMaster.getDrugType());
        requestObject.setDosageStrength(drugMaster.getDosageStrength());
        requestObject.setDrugName(drugMaster.getName());
        requestObject.setDrugNDC(drugMaster.getNdc());
        requestObject.setZipcode(drugMaster.getZipCode());

        Map<String, String> longitudeLatitude = priceController.constructLongLat(requestObject.getZipcode());
        String brandType = priceController.getBrandIndicator(requestObject).intern();
        if(brandType.equals("B")){
            requestObject.setDrugType("BRAND_WITH_GENERIC");
        }else{
            requestObject.setDrugType("GENERIC");
        }

        if (price.getProgramId() == 0) {
            requestObject.setProgram("insideRx");
            CompletableFuture<List<InsideRx>> inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);
            CompletableFuture.allOf(inside).join();
            try {
                List<InsideRx> insideRxPrices = inside.get();
                newPrice.setPrice(Double.parseDouble(insideRxPrices.get(0).getPrices().get(0).getPrice()));
                newPrice.setPharmacy(insideRxPrices.get(0).getPrices().get(0).getPharmacy().getName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 1) {
            requestObject.setProgram("usPharmacyCard");
            CompletableFuture<List<DrugNAP2>> usPharmacy = apiService2.constructUsPharmacy(requestObject);

            CompletableFuture.allOf(usPharmacy).join();
            try {
                List<DrugNAP2> usPharmacyPrices = usPharmacy.get();
                drugMaster = drugMasterRepository.findById(drugMaster.getId()).get();
                drugMaster.setDosageUOM(usPharmacyPrices.get(0).getDosage().getDosageUOM());
                drugMaster = drugMasterRepository.save(drugMaster);
                newPrice.setPrice(Double.parseDouble(usPharmacyPrices.get(0).getPriceList().get(0).getDiscountPrice()));
                newPrice.setPharmacy(usPharmacyPrices.get(0).getPriceList().get(0).getPharmacy().getPharmacyName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 2) {
            CompletableFuture<List<Drugs>> wellRxFuture = apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
            requestObject.setProgram("wellRx");
            CompletableFuture.allOf(wellRxFuture).join();
            try {
                List<Drugs> wellRx = wellRxFuture.get();
                newPrice.setPrice(Double.parseDouble(wellRx.get(0).getPrice()));
                newPrice.setPharmacy(wellRx.get(0).getPharmacyName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 3) {
            CompletableFuture<LocatedDrug> medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
            requestObject.setProgram("medImpact");
            CompletableFuture.allOf(medImpactFuture).join();
            try {
                LocatedDrug locatedDrug = medImpactFuture.get();
                newPrice.setPrice(Double.parseDouble(locatedDrug.getPricing().getPrice()));
                newPrice.setPharmacy(locatedDrug.getPharmacy().getName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 4) {
            CompletableFuture<PharmacyPricings> singleCareFuture = apiService.getSinglecarePrices(requestObject);
            requestObject.setProgram("medImpact");
            CompletableFuture.allOf(singleCareFuture).join();
            try {
                PharmacyPricings singleCarePrice = singleCareFuture.get();

                newPrice.setPrice(Double.parseDouble(singleCarePrice.getPrices().get(0).getPrice()));
                newPrice.setPharmacy(singleCarePrice.getPharmacy().getName());

            } catch (Exception e) {
                return null;
            }
        } else if (price.getProgramId() == 5) {
            CompletableFuture<Blink> blinkFuture = null;
            blinkFuture = apiService3.getBlinkPharmacyPrice(requestObject);

            Blink blink = blinkFuture.get();
            try {
                newPrice.setPrice(Double.parseDouble(blink.getPrice().getLocal().getRaw_value()));
                newPrice.setPharmacy("Blink");
            } catch (Exception e) {
                return null;
            }

        }

        return newPrice;


    }
    public String getPharmacyPrice(String providerName, DrugMaster drugMaster, RequestObject requestObject, Map<String, String> longitudeLatitude) {

        try {
            String brandType = drugMaster.getDrugType();
            if (providerName.equals("InsideRx")) {
                requestObject.setProgram("insideRx");
                CompletableFuture<List<InsideRx>> inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);
                CompletableFuture.allOf(inside).join();
                List<InsideRx> insideRxPrices = inside.get();

                return insideRxPrices.get(0).getPrices().get(0).getPrice();

            } else if (providerName.equals("U.S Pharmacy Card")) {
                CompletableFuture<List<DrugNAP2>> usPharmacy = apiService2.constructUsPharmacy(requestObject);
                requestObject.setProgram("usPharmacyCard");
                //      requestObject.setDrugType("BRAND_WITH_GENERIC");
                CompletableFuture.allOf(usPharmacy).join();
                try {
                    List<DrugNAP2> usPharmacyPrices = usPharmacy.get();
                    if (drugMaster.getDosageUOM() == null || drugMaster.getDosageUOM().equals("")) {
                        drugMaster = drugMasterRepository.findById(drugMaster.getId()).get();
                        drugMaster.setDosageUOM(usPharmacyPrices.get(0).getDosage().getDosageUOM());
                        drugMasterRepository.save(drugMaster);
                    }
                    return usPharmacyPrices.get(0).getPriceList().get(0).getDiscountPrice();
                } catch (Exception e) {
                    return "";
                }

            } else if (providerName.equals("WellRx")) {
                if (brandType.equals("BRAND_WITH_GENERIC")) {
                    brandType = "B";
                } else {
                    brandType = "G";
                }
                CompletableFuture<List<Drugs>> wellRxFuture = apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
                requestObject.setProgram("wellRx");
                CompletableFuture.allOf(wellRxFuture).join();
                List<Drugs> wellRx = wellRxFuture.get();
                return wellRx.get(0).getPrice() + " ";

            } else if (providerName.equals("MedImpact")) {
//                requestObject.setDrugType("BRAND_WITH_GENERIC");
//                brandType = "B";
                CompletableFuture<LocatedDrug> medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
                requestObject.setProgram("medImpact");

                CompletableFuture.allOf(medImpactFuture).join();
                LocatedDrug locatedDrug = medImpactFuture.get();

                return locatedDrug.getPricing().getPrice();

            } else if (providerName.equals("SingleCare")) {
                CompletableFuture<PharmacyPricings> singleCareFuture = apiService.getSinglecarePrices(requestObject);
                requestObject.setProgram("singleCare");
                CompletableFuture.allOf(singleCareFuture).join();
                PharmacyPricings singleCarePrice = singleCareFuture.get();

                return singleCarePrice.getPrices().get(0).getPrice();

            } else if (providerName.equals("InsideRx")) {
                CompletableFuture<Blink> blinkFuture = null;
                blinkFuture = apiService3.getBlinkPharmacyPrice(requestObject);

                Blink blink = blinkFuture.get();
                try {
                    return blink.getPrice().getLocal().getRaw_value();
                } catch (Exception e) {
                    return "";
                }

            }
            return " ";
        } catch (Exception e) {
            return " ";
        }
    }

    @GetMapping("/prices/get/all")
     List<Price> getPrices() {
        return priceRepository.findAll();
    }

     List<Price> getReportPrices(Report lastReport) {
        List<Report_Drugs> report_drugs = new ArrayList<>();
        try {
            report_drugs = reportDrugsRepository.findByReportId(lastReport.getId());
        } catch (Exception e) {

        }
        List<Price> prices = new ArrayList<>();

        for (Report_Drugs report_drug : report_drugs) {

            prices.add(priceRepository.findById(report_drug.getPriceId()).get());
        }
        return prices;
    }

    private Price savePrice(Price p) {
        if (p == null) {
            return null;
        }
        p.setCreatedat(new Date());
        return priceRepository.save(p);
    }
    public List<Price> addPrices(RequestObject requestObject, DrugMaster drugMaster) throws Throwable {
        PricesAndMaster details = drugMasterController.getDetails(requestObject, drugMaster);
        List<Price> addedPrices = new ArrayList<>();

        for (Price price : details.getPrices()) {
            addedPrices.add(savePrice(price));
        }
        Map<Integer,Double> providerPrices = new HashMap<>();

        Double lowestPrice = addedPrices.get(0).getPrice();
        Double averagePrice;
        Double sum= 0.0;
        int count = 0;
        for(Price p2 : addedPrices) {

            if(p2 == null ){

            }else{
                count++;
                sum = sum+p2.getPrice();
                if(p2.getPrice() <= lowestPrice){
                    lowestPrice = p2.getPrice();
                }

            }


        }
        averagePrice = sum/ count;

        for(Price p2 : addedPrices){
            if(p2 != null) {
                p2.setRecommendedPrice(lowestPrice);
                p2.setAveragePrice(averagePrice);
                priceRepository.save(p2);
            }
        }

        return addedPrices;
    }



}
