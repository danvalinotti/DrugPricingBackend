package com.galaxe.drugpriceapi.web.nap.controller;

import com.galaxe.drugpriceapi.model.Drug;
import com.galaxe.drugpriceapi.model.DrugNAP2;
import com.galaxe.drugpriceapi.model.Pharmacy;
import com.galaxe.drugpriceapi.model.PriceList;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugMasterRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugRequestRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugMaster;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugRequest;
import com.galaxe.drugpriceapi.web.nap.wellRx.*;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class APIClient2 {

    @Autowired
    DrugMasterRepository drugMasterRepository;

    private Gson gson = new Gson();

    private List<Drugs> drugs = new ArrayList<>();

    private Comparator<Drugs> constructWellRxComparator = null;

    private Comparator<PriceList> constructUsDrugComparator = null;

    private Map<String, WellRxPostObject> wellRxPostObjectMap = new HashMap<>();

    private Map<String, List<Strengths>> wellRxDrugGSNMap = new HashMap<>();

    private final String COOKIE_WELLRX = "ASP.NET_SessionId=b3krzxvpcoqy3yvkmmxysdad; __RequestVerificationToken=oRmuCHDrNMEqKZg9UV3r4iIDsfrhl8ufDkRjv-iQdLL0vK1mMcjBvwWRck8WKKLUEGrnxNjcOiG3UkpEjNMx0AzA_p81; wrxBannerID=1; _ga=GA1.2.1291111346.1555693895; _gid=GA1.2.1161917873.1555693895; _gcl_au=1.1.1411719143.1555693895; _fbp=fb.1.1555693895438.2092435015; b1pi=!CMbSNvIHLL2vAYwvLnpW7/Jj8QPM1+xdT0mf6+N2Vks4Ivb0dySAGjF6u88OryJxc2EHkscC+BoJkuk=; _gat=1";
    private final String COOKIE_WELLRX2 = "_ga=GA1.2.536278151.1556140629; _gcl_au=1.1.16835471.1556140629; _fbp=fb.1.1556140629715.1934721103; ASP.NET_SessionId=0ti2s11351uorufof45ymctu; __RequestVerificationToken=rdkSym5WxayIvoYy37bFSZd1owaTJqlu9u0pJokH-dlVTXGZYwY9eg9RFrfeqdP_xmzgpyoBFXqYsm1lMv9Kk3d02PQ1; _gid=GA1.2.1463898530.1563455908; _hjIncludedInSample=1; wrxBannerID=3; _gat=1; b1pi=!7Me/iyTCp0tP1KwvLnpW7/Jj8QPM145xnWUzspQDwUQeYGpFVcyf4wxN/DwIs7q5bElV8+jz6F+GtP0=";

    @Autowired
    DrugRequestRepository drugRequestRepository;

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<Drugs>> getWellRxDrugInfo(RequestObject requestObject, Map<String, String> longitudeLatitude, String brand_indicator) {
        int drugId = 0;

        try {

            drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0).getId();

            List<DrugRequest>drugRequests= drugRequestRepository.findByDrugIdAndProgramId(drugId,2);
            if(drugRequestRepository.findByDrugIdAndProgramId(drugId,2).size() != 0){

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId,2).get(0);
                if(drugRequest.getDrugName() == null || drugRequest.getDrugName().equals("")){
                    drugRequest.setDrugName(requestObject.getDrugName());
                    drugRequestRepository.save(drugRequest);
                }
                String wellRxSpecificDrugResponseStr = getWellRxResult(drugRequest);
                List<Drugs> wellRxSpecificDrugs = gson.fromJson(wellRxSpecificDrugResponseStr, WellRx.class).getDrugs();
                if (!CollectionUtils.isEmpty(wellRxSpecificDrugs)) {
                    if (constructWellRxComparator == null)
                        constructWellRxComparator = constructWellRxComparator();

                    Collections.sort(wellRxSpecificDrugs, constructWellRxComparator);
                    DrugMaster updatedDrug = drugMasterRepository.findById(drugId).get();
                    updatedDrug.setGsn(wellRxSpecificDrugs.get(0).getGSN());
                    drugMasterRepository.save(updatedDrug);
                    return CompletableFuture.completedFuture(wellRxSpecificDrugs);
                }

            }else{

            }
        }catch (Exception ex){

        }

        List<Strengths> strengths = null;
        // DrugMaster drugMaster2 = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(),requestObject.getQuantity()).get(0);
        String requestedDrug = requestObject.getDrugName().toUpperCase()
                .replace("/", "-")
                .replace("WITH PUMP", "")
                .replace("PUMP", "")
                .replace("VAGINAL","")
                .replace(" PEN","")
                .replace("PATCH","").intern();
        requestedDrug = requestedDrug.trim();
        String drugName = requestObject.getDrugName();

        requestObject.setDrugName(requestedDrug);
//        if (wellRxDrugGSNMap.containsKey(requestedDrug)) {
//
//            strengths = wellRxDrugGSNMap.get(requestedDrug);
//
//        } else {
//            String str = getWellRxOutputString(requestObject, longitudeLatitude).intern();
//            requestObject.setDrugName(drugName);
//            WellRx wellRxFirstAPIResp = gson.fromJson(str, WellRx.class);

        String str = getWellRxOutputString4(requestObject, longitudeLatitude).intern();
        requestObject.setDrugName(drugName);
        WellRx wellRxFirstAPIResp = gson.fromJson(str, WellRx.class);
        String dose = requestObject.getDosageStrength().toUpperCase();
        if(dose.contains("PUMP")){
            dose = "PUMP";
        }
        if (wellRxFirstAPIResp.getForms().size() > 0) {
            // wellRxFirstAPIResp

            for (int i = 0; i < wellRxFirstAPIResp.getForms().size(); i++) {

                String drugForm = wellRxFirstAPIResp.getForms().get(i).getForm().toUpperCase();
                System.out.println("DRUG FORM BEFORE "+drugForm);
                try {
                    drugForm = drugForm.replace(drugForm.substring(drugForm.indexOf("("), drugForm.indexOf(")")+1),"");
                    drugForm = drugForm.trim();

                    System.out.println("DRUG FORM"+drugForm);
                }catch(Exception ex){

                }

                if(drugForm.contains("PUMP")){
                    drugForm = "PUMP";
                }
                System.out.println(dose);
                System.out.println("DRUG FORM"+drugForm);

                if(dose.contains(drugForm)){
                    System.out.println("HERE");

                requestObject.setGSN(wellRxFirstAPIResp.getForms().get(i).getGSN());
                String str2 = getWellRxOutputString2(requestObject, longitudeLatitude, brand_indicator).intern();

                WellRx wellRxFirstAPIResp2 = gson.fromJson(str2, WellRx.class);

                CompletableFuture<List<Drugs>> result = getSpecWellRxDrug(wellRxFirstAPIResp2, requestedDrug, new ArrayList<>(), requestObject, longitudeLatitude, brand_indicator);
                if (result.join().size() != 0) {

                    //INSERT DRUGREQUESET
                    try {
                        drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0).getId();
                        if (drugRequestRepository.findByDrugIdAndProgramId(drugId, 2).size() == 0) {
                            DrugRequest drugRequest = new DrugRequest();
                            drugRequest.setProgramId(2);
                            Drugs savedDrug = result.join().get(0);
                            drugRequest.setGsn(savedDrug.getGSN());
                            drugRequest.setLatitude(longitudeLatitude.get("latitude"));
                            drugRequest.setLongitude(longitudeLatitude.get("longitude"));
                            drugRequest.setQuantity(savedDrug.getQty());
                            drugRequest.setBrandIndicator(brand_indicator);
                            drugRequest.setDrugName(savedDrug.getDrugName());
                            drugRequestRepository.save(drugRequest);
                        }else{
                            DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId, 2).get(0);
                            drugRequest.setProgramId(2);
                            Drugs savedDrug = result.join().get(0);
                            drugRequest.setGsn(savedDrug.getGSN());
                            drugRequest.setLatitude(longitudeLatitude.get("latitude"));
                            drugRequest.setLongitude(longitudeLatitude.get("longitude"));
                            drugRequest.setQuantity(savedDrug.getQty());
                            drugRequest.setBrandIndicator(brand_indicator);
                            drugRequest.setDrugName(savedDrug.getDrugName());
                            drugRequestRepository.save(drugRequest);
                        }
                    } catch (Exception ex) {

                    }
                    return result;
                }
                }
            }

        }


        if (!CollectionUtils.isEmpty(wellRxFirstAPIResp.getStrengths())) {
            wellRxDrugGSNMap.put(requestedDrug, wellRxFirstAPIResp.getStrengths());
            strengths = wellRxFirstAPIResp.getStrengths();

        } else {
           // return CompletableFuture.completedFuture(drugs);
        }
//        }


        if (strengths != null) {

            WellRxSpecifDrugPost obj = new WellRxSpecifDrugPost();
            String dosageStrength = requestObject.getDosageStrength().toUpperCase().replace("/24" , "").trim().replaceAll("[A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|%|\\s]", "").trim().intern();
            String dosageStrength2 = requestObject.getDosageStrength().toUpperCase().replace("/24" , "").trim().replaceAll("[A-Z|a-z|/|(|)|-|MG|MCG|ML|MG-MCG|%]", "").trim().intern();


            strengths.forEach(strength -> {
                List<String> words = new ArrayList<>();
                Collections.addAll(words, dosageStrength2.split("[-\\s\\\\]"));
                words.removeIf(s -> {
                    try {
                        Double.parseDouble(s);
                    } catch (Exception e) {
                        try {
                            Integer.parseInt(s);
                        } catch (Exception ex) {
                            return true;
                        }
                        return false;
                    }
                    return false;
                });

                String dosage = "";
                if (requestObject.getDosageStrength().contains("day")) {
                    dosage = strength.getStrength().toUpperCase().replaceAll("[-A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|-|%|\\s]", "").trim().intern();
                    dosage = dosage.replace("24", "");
                } else {
                    dosage = strength.getStrength().toUpperCase().replaceAll("[-A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|-|%|\\s]", "").trim().intern();
                }
//                System.out.println("dosage"+dosage);
//                System.out.println("Words"+words);
                if (words.size() > 1) {
                    try {
                        if (Double.parseDouble(dosage) == Double.parseDouble(words.get(0))) {

                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(1)) == Double.parseDouble(words.get(0)) / 1000) {

                            } else {
                                requestObject.setQuantity(Double.parseDouble(words.get(1)));
                            }

                            return;
                        }
                    } catch (Exception ex) {
                        if (dosage.equalsIgnoreCase(words.get(0))) {
                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(1)) == Double.parseDouble(words.get(0)) / 1000) {

                            } else {
                                requestObject.setQuantity(Double.parseDouble(words.get(1)));
                            }

                            return;
                        }
                    }
                    try {
                        if (Double.parseDouble(dosage) == Double.parseDouble(words.get(1))) {

                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(0)) == Double.parseDouble(words.get(1)) / 1000) {

                            } else {
                                requestObject.setQuantity(Double.parseDouble(words.get(0)));
                            }
                            return;
                        }
                    } catch (Exception ex) {
                        if (dosage.equalsIgnoreCase(words.get(1))) {
                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(0)) == Double.parseDouble(words.get(1)) / 1000) {

                            } else {
                                requestObject.setQuantity(Double.parseDouble(words.get(0)));
                            }
                            return;
                        }
                    }
                    if (dosage.equalsIgnoreCase(frontwards(words))) {
                        obj.setGSN(strength.getGSN());
                        //requestObject.setQuantity(Double.parseDouble(words.get(0)));
                        return;
                    }
                    if (dosage.equalsIgnoreCase(backwards(words))) {
                        obj.setGSN(strength.getGSN());
                        //requestObject.setQuantity(Double.parseDouble(words.get(0)));
                        return;
                    }
                }
                if (dosage.equalsIgnoreCase(dosageStrength)) {
                    obj.setGSN(strength.getGSN());
                    return;
                }
//                if (dosageStrength.contains(dosage)) {
//                    obj.setGSN(strength.getGSN());
//
//                }

            });
//            System.out.println("objGSN" +obj.getGSN());
            obj.setBgIndicator(brand_indicator);
            obj.setLat(longitudeLatitude.get("latitude"));
            obj.setLng(longitudeLatitude.get("longitude"));
            obj.setNumdrugs("1");
            obj.setQuantity(String.valueOf(requestObject.getQuantity()));
            obj.setBReference(requestObject.getDrugName().replace("Patch","").trim());
            obj.setNcpdps("null");
            if (obj.getGSN() != null && !obj.getGSN().isEmpty()) {
                String wellRxSpecificDrugResponseStr = getWellRxDrugSpecificOutput(obj, requestObject).intern();
                List<Drugs> wellRxSpecificDrugs = gson.fromJson(wellRxSpecificDrugResponseStr, WellRx.class).getDrugs();
          /////
                if (!CollectionUtils.isEmpty(wellRxSpecificDrugs)) {
                    if (constructWellRxComparator == null)
                        constructWellRxComparator = constructWellRxComparator();

                    Collections.sort(wellRxSpecificDrugs, constructWellRxComparator);
                    try {
                        DrugMaster drugMaster = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0);
                        drugMaster.setGsn(obj.getGSN());
                        drugMasterRepository.save(drugMaster);

                        if (drugRequestRepository.findByDrugIdAndProgramId(drugId, 1).size() > 0) {
                            DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugMaster.getId(),2).get(0);
                            drugRequest.setGsn(wellRxSpecificDrugs.get(0).getGSN());
                            drugRequestRepository.save(drugRequest);
                            try {
                                DrugRequest drugRequest2 = drugRequestRepository.findByDrugIdAndProgramId(drugMaster.getId(), 3).get(0);
                                drugRequest2.setGsn(wellRxSpecificDrugs.get(0).getGSN());
                                drugRequestRepository.save(drugRequest2);
                            }catch (Exception ex){

                            }
                        }

                    } catch (Exception ex) {

                    }
//                    System.out.println("SPecific drug price"+wellRxSpecificDrugs.get(0).getPrice());
                    return CompletableFuture.completedFuture(wellRxSpecificDrugs);
                }
            }
        }
        if (drugs.size() == 0 && wellRxFirstAPIResp.getForms().size() >= 2) {
            // wellRxFirstAPIResp

            for (int i = 1; i < wellRxFirstAPIResp.getForms().size(); i++) {
                requestObject.setGSN(wellRxFirstAPIResp.getForms().get(i).getGSN());
                String str2 = getWellRxOutputString2(requestObject, longitudeLatitude, brand_indicator).intern();

                WellRx wellRxFirstAPIResp2 = gson.fromJson(str2, WellRx.class);

                CompletableFuture<List<Drugs>> result = getSpecWellRxDrug(wellRxFirstAPIResp2, requestedDrug, new ArrayList<>(), requestObject, longitudeLatitude, brand_indicator);
                if (result.join().size() != 0) {
                    try {
                        DrugMaster drugMaster = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0);
                        drugMaster.setGsn(result.join().get(0).getGSN());
                        drugMasterRepository.save(drugMaster);
                    } catch (Exception ex) {

                    }
                    return result;
                }
            }

        }

        return CompletableFuture.completedFuture(drugs);
    }

    private String backwards(List<String> words) {
        String s = "";
        try {
            s = words.get(1) + words.get(0);
        } catch (Exception ex) {
            return words.get(0);
        }
        return s;
    }

    private String frontwards(List<String> words) {
        String s = "";
        try {
            s = words.get(0) + words.get(1);
        } catch (Exception ex) {
            return words.get(0);
        }
        return s;
    }

    public CompletableFuture<List<Drugs>> getSpecWellRxDrug(WellRx wellRxFirstAPIResp, String requestedDrug, List<Strengths> strengths, RequestObject requestObject, Map<String, String> longitudeLatitude, String brand_indicator) {

        if (!CollectionUtils.isEmpty(wellRxFirstAPIResp.getStrengths())) {
            wellRxDrugGSNMap.put(requestedDrug, wellRxFirstAPIResp.getStrengths());
            strengths = wellRxFirstAPIResp.getStrengths();
        } else {
            return CompletableFuture.completedFuture(drugs);
        }
//        }


        if (strengths != null) {

            WellRxSpecifDrugPost obj = new WellRxSpecifDrugPost();
            String dosageStrength = requestObject.getDosageStrength().toUpperCase().replaceAll("[A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|%|\\s]", "").trim().intern();

            String dosageStrength2 = requestObject.getDosageStrength().toUpperCase().replaceAll("[A-Z|a-z|/|(|)|-|MG|MCG|ML|MG-MCG|%]", "").trim().intern();
            strengths.forEach(strength -> {
                List<String> words = new ArrayList<>();
                Collections.addAll(words, dosageStrength2.split("[-\\s\\\\]"));
                words.removeIf(s -> {
                    try {
                        Double.parseDouble(s);
                    } catch (Exception e) {
                        try {
                            Integer.parseInt(s);
                        } catch (Exception ex) {
                            return true;
                        }
                        return false;
                    }
                    return false;
                });

                String dosage = "";
                if (requestObject.getDosageStrength().contains("day")) {

                    dosage = strength.getStrength().toUpperCase().replaceAll("[-A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|-|%|\\s]", "").trim().intern();
                    dosage = dosage.replace("24", "");
                } else {
                    dosage = strength.getStrength().toUpperCase().replaceAll("[-A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|-|%|\\s]", "").trim().intern();
                }
                if (words.size() > 1) {
                    try {
                        if (Double.parseDouble(dosage) == Double.parseDouble(words.get(0))) {
                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(1)) == Double.parseDouble(words.get(0)) / 1000) {

                            } else {
                                requestObject.setQuantity(Double.parseDouble(words.get(1)));
                            }

                            return;
                        }
                    } catch (Exception ex) {
                        if (dosage.equalsIgnoreCase(words.get(0))) {
                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(1)) == Double.parseDouble(words.get(0)) / 1000) {

                            } else {
                                requestObject.setQuantity(Double.parseDouble(words.get(1)));
                            }

                            return;
                        }
                    }
                    try {
                        if (Double.parseDouble(dosage) == Double.parseDouble(words.get(1))) {
                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(0)) == Double.parseDouble(words.get(1)) / 1000) {

                            } else {
                                requestObject.setQuantity(Double.parseDouble(words.get(0)));
                            }
                            return;
                        }
                    } catch (Exception ex) {
                        if (dosage.equalsIgnoreCase(words.get(1))) {
                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(0)) == Double.parseDouble(words.get(1)) / 1000) {

                            } else {
                                requestObject.setQuantity(Double.parseDouble(words.get(0)));
                            }
                            return;
                        }
                    }
                    if (dosage.equalsIgnoreCase(frontwards(words))) {
                        obj.setGSN(strength.getGSN());
                        //requestObject.setQuantity(Double.parseDouble(words.get(0)));
                        return;
                    }
                    if (dosage.equalsIgnoreCase(backwards(words))) {
                        obj.setGSN(strength.getGSN());
                        //requestObject.setQuantity(Double.parseDouble(words.get(0)));
                        return;
                    }
                }
                if (dosage.equalsIgnoreCase(dosageStrength)) {
                    obj.setGSN(strength.getGSN());
                    return;
                }
//                if (dosageStrength.contains(dosage)) {
//                    obj.setGSN(strength.getGSN());
//
//                }

            });

            obj.setBgIndicator(brand_indicator);
            obj.setLat(longitudeLatitude.get("latitude"));
            obj.setLng(longitudeLatitude.get("longitude"));
            obj.setNumdrugs("1");
            obj.setQuantity(String.valueOf(requestObject.getQuantity()));
            obj.setBReference(requestObject.getDrugName());
            obj.setNcpdps("null");

            if (obj.getGSN() != null && !obj.getGSN().isEmpty()) {
                String wellRxSpecificDrugResponseStr = getWellRxDrugSpecificOutput2(obj).intern();
                List<Drugs> wellRxSpecificDrugs = gson.fromJson(wellRxSpecificDrugResponseStr, WellRx.class).getDrugs();

                if (!CollectionUtils.isEmpty(wellRxSpecificDrugs)) {
                    if (constructWellRxComparator == null)
                        constructWellRxComparator = constructWellRxComparator();

                    Collections.sort(wellRxSpecificDrugs, constructWellRxComparator);
                    return CompletableFuture.completedFuture(wellRxSpecificDrugs);
                }
            }
        }
        return CompletableFuture.completedFuture(drugs);
    }


    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<DrugNAP2>> constructUsPharmacy(RequestObject requestObject) {

        String drugType = requestObject.getDrugType();
        if (drugType.equals("B")) {
            drugType = "BRAND_WITH_GENERIC";
        } else if (drugType.equals("G")) {
            drugType = "GENERIC";
        }

        WebClient webClient;
        int drugId = 0;
        try {
            drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0).getId();

            List<DrugRequest>drugRequests= drugRequestRepository.findByDrugIdAndProgramId(drugId,1);

            if(drugRequestRepository.findByDrugIdAndProgramId(drugId,1).size() != 0){

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId,1).get(0);

                webClient= WebClient.create("https://api.uspharmacycard.com/drug/price/147/020982/" + drugRequest.getZipcode()
                        + "/" + drugRequest.getNdc() + "/" + drugRequest.getDrugName() + "/" + drugRequest.getBrandIndicator() + "/" + drugRequest.getQuantity() + "/10");

            }else{
                webClient= WebClient.create("https://api.uspharmacycard.com/drug/price/147/020982/" + requestObject.getZipcode()
                        + "/" + requestObject.getDrugNDC() + "/" + requestObject.getDrugName() + "/" + drugType + "/" + requestObject.getQuantity() + "/10");

            }
        }catch (Exception ex){
            webClient= WebClient.create("https://api.uspharmacycard.com/drug/price/147/020982/" + requestObject.getZipcode()
                    + "/" + requestObject.getDrugNDC() + "/" + requestObject.getDrugName() + "/" + drugType + "/" + requestObject.getQuantity() + "/10");
        }


        List<DrugNAP2> usPharmacies = new ArrayList<>();

        try {
            usPharmacies = webClient
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .flatMapMany(clientResponse -> clientResponse.bodyToFlux(DrugNAP2.class))
                    .collectList().block();
            try {

                if (drugRequestRepository.findByDrugIdAndProgramId(drugId, 1).size() == 0) {
                    DrugRequest drugRequest = new DrugRequest();
                    drugRequest.setProgramId(1);
                    drugRequest.setDrugId(drugId);
                    drugRequest.setZipcode(requestObject.getZipcode());
                    drugRequest.setNdc(requestObject.getDrugNDC());
                    drugRequest.setDrugName(requestObject.getDrugName());
                    drugRequest.setBrandIndicator(drugType);
                    drugRequest.setQuantity(requestObject.getQuantity() + "");
                    drugRequestRepository.save(drugRequest);
                }else{
                    DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId, 1).get(0);
                    drugRequest.setProgramId(1);
                    drugRequest.setDrugId(drugId);
                    drugRequest.setZipcode(requestObject.getZipcode());
                    drugRequest.setNdc(requestObject.getDrugNDC());
                    drugRequest.setDrugName(requestObject.getDrugName());
                    drugRequest.setBrandIndicator(drugType);
                    drugRequest.setQuantity(requestObject.getQuantity() + "");
                    drugRequestRepository.save(drugRequest);
                }
            } catch (Exception ex) {

            }
            if (!CollectionUtils.isEmpty(usPharmacies.get(0).getPriceList())) {
                List<PriceList> priceList = usPharmacies.get(0).getPriceList();


                if (constructUsDrugComparator == null)
                    constructUsDrugComparator = constructUsDrugComparator();

                Collections.sort(priceList, constructUsDrugComparator);
            }
        } catch (Exception ex) {

        }

        return CompletableFuture.completedFuture(usPharmacies);
    }


    String getWellRxOutputString(RequestObject requestObject, Map<String, String> longitudeLatitude) {
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-drugs");
        WellRxPostObject w = constructWellRxPostObject(requestObject, longitudeLatitude);
        return webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Referer", "https://www.wellrx.com/prescriptions/lipitor/somerset,%20nj%2008873,%20usa")
                .header("Cookie", COOKIE_WELLRX)
                .header("X-Requested-With", "XMLHttpRequest")
                .body(Mono.just(constructWellRxPostObject(requestObject, longitudeLatitude)), WellRxPostObject.class)
                .retrieve().bodyToMono(String.class)
                .block();

    }

    String getWellRxOutputString4(RequestObject requestObject, Map<String, String> longitudeLatitude) {
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-brand-generic");
        BrandGenericPost brandGenericPost = new BrandGenericPost();
        if (requestObject.getDrugType().equals("BRAND_WITH_GENERIC")) {
            brandGenericPost.setBgIndicator("B");
        } else if (requestObject.getDrugType().equals("GENERIC")) {
            brandGenericPost.setBgIndicator("G");
        } else {
            brandGenericPost.setBgIndicator(requestObject.getDrugType());
        }

        brandGenericPost.setDrugname(requestObject.getDrugName());
        brandGenericPost.setLat(longitudeLatitude.get("latitude"));
        brandGenericPost.setLng(longitudeLatitude.get("longitude"));
        brandGenericPost.setNcpdps("null");
        brandGenericPost.setNumdrugs("1");

        try {
            int drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0).getId();
            if (drugRequestRepository.findByDrugIdAndProgramId(drugId, 2).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setProgramId(2);
                drugRequest.setDrugId(drugId);
                drugRequest.setBrandIndicator(brandGenericPost.getBgIndicator());
                drugRequest.setDrugName(brandGenericPost.getDrugname());
                drugRequest.setLatitude(brandGenericPost.getLat());
                drugRequest.setLongitude(brandGenericPost.getLng());
                drugRequestRepository.save(drugRequest);
            }else{

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId, 2).get(0);
                drugRequest.setProgramId(2);
                drugRequest.setDrugId(drugId);
                drugRequest.setBrandIndicator(brandGenericPost.getBgIndicator());
                drugRequest.setDrugName(brandGenericPost.getDrugname());
                drugRequest.setLatitude(brandGenericPost.getLat());
                drugRequest.setLongitude(brandGenericPost.getLng());
                drugRequestRepository.save(drugRequest);
            }
        } catch (Exception ex) {

        }
        return webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Referer", "https://www.wellrx.com/prescriptions/lipitor/somerset,%20nj%2008873,%20usa")
                .header("Cookie", COOKIE_WELLRX)
                .header("X-Requested-With", "XMLHttpRequest")
                .body(Mono.just(brandGenericPost), BrandGenericPost.class)
                .retrieve().bodyToMono(String.class)
                .block();

    }
    String getWellRxResult(DrugRequest drugRequest) {
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-brand-generic");
        BrandGenericPost brandGenericPost = new BrandGenericPost();
        if (drugRequest.getBrandIndicator().equals("BRAND_WITH_GENERIC")) {
            brandGenericPost.setBgIndicator("B");
        } else if (drugRequest.getBrandIndicator().equals("GENERIC")) {
            brandGenericPost.setBgIndicator("G");
        } else {
            brandGenericPost.setBgIndicator(drugRequest.getBrandIndicator());
        }

        brandGenericPost.setDrugname(drugRequest.getDrugName());
        brandGenericPost.setLat(drugRequest.getLatitude());
        brandGenericPost.setLng(drugRequest.getLongitude());
        brandGenericPost.setNcpdps("null");
        brandGenericPost.setNumdrugs("1");


        return webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Referer", "https://www.wellrx.com/prescriptions/lipitor/somerset,%20nj%2008873,%20usa")
                .header("Cookie", COOKIE_WELLRX)
                .header("X-Requested-With", "XMLHttpRequest")
                .body(Mono.just(brandGenericPost), BrandGenericPost.class)
                .retrieve().bodyToMono(String.class)
                .block();

    }

    String getWellRxOutputString2(RequestObject requestObject, Map<String, String> longitudeLatitude, String brand_indicator) {
//        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-specific-drug");
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-specific-drug");

        WellRxGSNSearch wellRxGSNSearch = new WellRxGSNSearch();
        wellRxGSNSearch.setGSN(requestObject.getGSN());
        wellRxGSNSearch.setLat(longitudeLatitude.get("latitude"));
        wellRxGSNSearch.setLng(longitudeLatitude.get("longitude"));
        wellRxGSNSearch.setNumdrugs("1");
        wellRxGSNSearch.setQuantity(requestObject.getQuantity() + "");
        wellRxGSNSearch.setBgIndicator(brand_indicator);
        wellRxGSNSearch.setbReference(requestObject.getDrugName());
        wellRxGSNSearch.setNcpdps("null");

        Mono<String> s = webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Host", "www.wellrx.com")
                .header("Referer", "https://www.wellrx.com/prescriptions/humatrope/somerset")
                .header("Cookie", COOKIE_WELLRX2)
                .header("X-Requested-With", "XMLHttpRequest")
                .body(Mono.just(wellRxGSNSearch), WellRxGSNSearch.class)
                .retrieve().bodyToMono(String.class);

        return s.block();

    }

    private String getWellRxDrugSpecificOutput(WellRxSpecifDrugPost wellRxSpecifDrugPost, RequestObject requestObject) {
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-specific-drug");

        try {
            int drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0).getId();
            if (drugRequestRepository.findByDrugIdAndProgramId(drugId, 2).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setGsn(wellRxSpecifDrugPost.getGSN());
                drugRequest.setBrandIndicator(wellRxSpecifDrugPost.getBgIndicator());
                drugRequest.setDrugName(wellRxSpecifDrugPost.getBReference());
                drugRequest.setLatitude(wellRxSpecifDrugPost.getLat());
                drugRequest.setLongitude(wellRxSpecifDrugPost.getLng());
                drugRequest.setQuantity(wellRxSpecifDrugPost.getQuantity());
                drugRequest.setProgramId(2);
                drugRequest.setDrugId(drugId);
                drugRequestRepository.save(drugRequest);
            }else{
                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId, 2).get(0) ;
                drugRequest.setGsn(wellRxSpecifDrugPost.getGSN());
                drugRequest.setBrandIndicator(wellRxSpecifDrugPost.getBgIndicator());
                drugRequest.setDrugName(wellRxSpecifDrugPost.getBReference());
                drugRequest.setLatitude(wellRxSpecifDrugPost.getLat());
                drugRequest.setLongitude(wellRxSpecifDrugPost.getLng());
                drugRequest.setQuantity(wellRxSpecifDrugPost.getQuantity());
                drugRequest.setProgramId(2);
                drugRequest.setDrugId(drugId);
                drugRequestRepository.save(drugRequest);
            }
        } catch (Exception ex) {

        }
        return webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Referer", "https://www.wellrx.com/prescriptions/lipitor/somerset,%20nj%2008873,%20usa")
                .header("Cookie", COOKIE_WELLRX)
                .header("X-Requested-With", "XMLHttpRequest")
                .body(Mono.just(wellRxSpecifDrugPost), WellRxSpecifDrugPost.class)
                .retrieve().bodyToMono(String.class)
                .block();

    }

    private String getWellRxDrugSpecificOutput2(WellRxSpecifDrugPost wellRxSpecifDrugPost) {
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-specific-drug");

        WellRxGSNSearch wellRxGSNSearch = new WellRxGSNSearch();
        wellRxGSNSearch.setGSN(wellRxSpecifDrugPost.getGSN());
        wellRxGSNSearch.setLat(wellRxSpecifDrugPost.getLat());
        wellRxGSNSearch.setLng(wellRxSpecifDrugPost.getLng());
        wellRxGSNSearch.setNumdrugs(wellRxSpecifDrugPost.getNumdrugs());
        wellRxGSNSearch.setQuantity(wellRxSpecifDrugPost.getQuantity());
        wellRxGSNSearch.setBgIndicator(wellRxSpecifDrugPost.getBgIndicator());
        wellRxGSNSearch.setbReference(wellRxSpecifDrugPost.getBReference());
        wellRxGSNSearch.setNcpdps(wellRxSpecifDrugPost.getNcpdps());

        Mono<String> s = webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Host", "www.wellrx.com")
                .header("Referer", "https://www.wellrx.com/prescriptions/humatrope/somerset")
                .header("Cookie", COOKIE_WELLRX2)
                .header("X-Requested-With", "XMLHttpRequest")
                .body(Mono.just(wellRxGSNSearch), WellRxGSNSearch.class)
                .retrieve().bodyToMono(String.class);
        String str = s.block();
        return str;

    }

    private WellRxPostObject constructWellRxPostObject(RequestObject requestObject, Map<String, String> longitudeLatitude) {

        if (wellRxPostObjectMap.containsKey(requestObject.getDrugName())) {

            wellRxPostObjectMap.get(requestObject.getDrugName()).setLat(longitudeLatitude.get("latitude"));
            wellRxPostObjectMap.get(requestObject.getDrugName()).setLng(longitudeLatitude.get("longitude"));

            return wellRxPostObjectMap.get(requestObject.getDrugName());
        } else {
            WellRxPostObject obj = new WellRxPostObject();
            obj.setDrugname(requestObject.getDrugName());
            obj.setLat(longitudeLatitude.get("latitude"));
            obj.setLng(longitudeLatitude.get("longitude"));
            obj.setQty("0");
            obj.setNumdrugs("1");
            obj.setNcpdps("null");
            wellRxPostObjectMap.put(requestObject.getDrugName(), obj);
            return obj;
        }
    }

    private Comparator<Drugs> constructWellRxComparator() {
        return (obj1, obj2) -> {
            Double price1 = Double.parseDouble(obj1.getPrice());
            Double price2 = Double.parseDouble(obj2.getPrice());

            return price1.compareTo(price2);
        };
    }

    private Comparator<PriceList> constructUsDrugComparator() {
        return (o1, o2) -> {
            Double p1 = Double.parseDouble(o1.getDiscountPrice());
            Double p2 = Double.parseDouble(o2.getDiscountPrice());

            if (p1 > p2) {
                return 1;
            } else if (p1 < p2) {
                return -1;
            } else {
                return 0;
            }
        };
    }

}
