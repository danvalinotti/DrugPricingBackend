package com.galaxe.drugpriceapi.web.nap.controller;

import com.galaxe.drugpriceapi.model.InsideRx;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrug;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrugStrength;
import com.galaxe.drugpriceapi.web.nap.medimpact.MedImpact;
import com.galaxe.drugpriceapi.web.nap.model.PostObject;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugMasterRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugRequestRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.goodRx.GoodRxResponse;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugMaster;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugRequest;
import com.galaxe.drugpriceapi.web.nap.singlecare.*;
import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.CollectionUtils;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class APIClient {

    private final String CSRF_TOKEN = "Hi6yGXfg-vppErZsd2KXvKmH9LxjPBNJeK48";

    private final String COOKIE = "_gcl_au=1.1.1639244140.1555443999; _fbp=fb.1.1555443999440.65320427; _ga=GA1.2.711100002.1555444000; _gid=GA1.2.317294123.1555444000; _hjIncludedInSample=1; _csrf=Z3iefjYKIjIUIEXBJgTix0BY; _gat_UA-113293481-1=1; geocoords=40.7473758%2C-74.05057520000003; AWSALB=6NBPPYHYpRwHG5ONO7yvFP6fzmSCfiDRLUr3FCKprscG4ld2CKg2lU+ZRCxhxrTF55clcMF7APSLyeZBhLeH2pv/9pzCIWt8u9lcfJfF8La8Z/eIpABRoF3orpJj";

    private final String INSIDERXURL = "https://insiderx.com/request/pharmacies";

    private final String SINGLECAREURL = "https://api.singlecare.com/services/v1_0/Public/PBMService.svc/GetTieredPricing";

    private Gson gson = new Gson();

    private LocatedDrug medImpactDrug = new LocatedDrug();

    private Comparator<com.galaxe.drugpriceapi.model.Prices> insideRxComparator = null;

    private Map<String, List<LocatedDrugStrength>> medImpactGSNMap = new HashMap<>();

    private Comparator<Prices> constructSigleCareComparator = null;

    @Autowired
    DrugRequestRepository drugRequestRepository;
    @Autowired
    DrugMasterRepository drugMasterRepository;

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<PharmacyPricings> getSinglecarePrices(@RequestBody RequestObject requestObject) {

        int drugId = 0;
        String str = "";
        try {
            drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity(),requestObject.getZipcode()).get(0).getId();

            List<DrugRequest> drugRequests = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 4);

            if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 4).size() != 0) {
                String json = gson.toJson(constructSinglecarePostObject(drugRequests.get(0)));
//                System.out.println("DrugSaved");
                if(drugRequests.get(0).getDrugName() == null || drugRequests.get(0).getDrugName().equals("")){
                    drugRequests.get(0).setDrugName(requestObject.getDrugName());
                    drugRequestRepository.save(drugRequests.get(0));
                }

                WebClient webClient = WebClient.create(SINGLECAREURL);
                str = webClient
                        .post()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(Mono.just(json), String.class)
                        .retrieve().bodyToMono(String.class).block();
                Singlecare singlecare = gson.fromJson(str, Singlecare.class);
                try {
//                    System.out.println("SINGLECARE");
//                    System.out.println(singlecare.getValue().getPharmacyPricings().get(0).getPrices().get(0));
                }catch (Exception ex){

                }
                if (singlecare.getValue() != null) {
                    if (!CollectionUtils.isEmpty(singlecare.getValue().getPharmacyPricings())) {

                        if (constructSigleCareComparator == null)
                            constructSigleCareComparator = constructSigleCareComparator();

                        Collections.sort(singlecare.getValue().getPharmacyPricings().get(0).getPrices(), constructSigleCareComparator);
                        return CompletableFuture.completedFuture((singlecare.getValue().getPharmacyPricings().get(0)));
                    }
                }
            }else{
                String json = gson.toJson(constructSinglecarePostObject(requestObject));

                WebClient webClient = WebClient.create(SINGLECAREURL);
                str = webClient
                        .post()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(Mono.just(json), String.class)
                        .retrieve().bodyToMono(String.class).block();
                Singlecare singlecare = gson.fromJson(str, Singlecare.class);
                try {
//                    System.out.println("SINGLECARE");
//                    System.out.println(singlecare.getValue().getPharmacyPricings().get(0).getPrices().get(0));
                }catch (Exception ex){

                }
                if (singlecare.getValue() != null) {
                    if (!CollectionUtils.isEmpty(singlecare.getValue().getPharmacyPricings())) {

                        if (constructSigleCareComparator == null)
                            constructSigleCareComparator = constructSigleCareComparator();

                        Collections.sort(singlecare.getValue().getPharmacyPricings().get(0).getPrices(), constructSigleCareComparator);
                        return CompletableFuture.completedFuture((singlecare.getValue().getPharmacyPricings().get(0)));
                    }
                }
            }
        }catch (Exception ex){
            String json = gson.toJson(constructSinglecarePostObject(requestObject));

            WebClient webClient = WebClient.create(SINGLECAREURL);
            str = webClient
                    .post()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(Mono.just(json), String.class)
                    .retrieve().bodyToMono(String.class).block();
            Singlecare singlecare = gson.fromJson(str, Singlecare.class);
            try {
//                System.out.println("SINGLECARE");
//                System.out.println(singlecare.getValue().getPharmacyPricings().get(0).getPrices().get(0));
            }catch (Exception e){

            }
            if (singlecare.getValue() != null) {
                if (!CollectionUtils.isEmpty(singlecare.getValue().getPharmacyPricings())) {

                    if (constructSigleCareComparator == null)
                        constructSigleCareComparator = constructSigleCareComparator();

                    Collections.sort(singlecare.getValue().getPharmacyPricings().get(0).getPrices(), constructSigleCareComparator);
                    return CompletableFuture.completedFuture((singlecare.getValue().getPharmacyPricings().get(0)));
                }
            }
        }
        String json = gson.toJson(constructSinglecarePostObject(requestObject));

        WebClient webClient = WebClient.create(SINGLECAREURL);
        str = webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(json), String.class)
                .retrieve().bodyToMono(String.class).block();
        Singlecare singlecare = gson.fromJson(str, Singlecare.class);
        try {
//            System.out.println("SINGLECARE");
//            System.out.println(singlecare.getValue().getPharmacyPricings().get(0).getPrices().get(0));
        }catch (Exception e){

        }
        if (singlecare.getValue() != null) {
            if (!CollectionUtils.isEmpty(singlecare.getValue().getPharmacyPricings())) {

                if (constructSigleCareComparator == null)
                    constructSigleCareComparator = constructSigleCareComparator();

                Collections.sort(singlecare.getValue().getPharmacyPricings().get(0).getPrices(), constructSigleCareComparator);
                return CompletableFuture.completedFuture((singlecare.getValue().getPharmacyPricings().get(0)));
            }
        }



//        System.out.println("null");
        return CompletableFuture.completedFuture(new PharmacyPricings());
    }

    private SinglecarePostObject constructSinglecarePostObject(DrugRequest drugRequest) {
        SinglecarePostObject object = new SinglecarePostObject();
        PValue Value = new PValue();
        Value.setNDC(drugRequest.getNdc());
        Value.setQuantity(String.valueOf(drugRequest.getQuantity()));
        Value.setZipCode(drugRequest.getZipcode());
        Value.setDistance(30);
        Value.setMaxResults(30);
        Value.setTenantId(0);
        Value.setNABP("");
        object.setValue(Value);

        return object;
    }


    private SinglecarePostObject constructSinglecarePostObject(RequestObject requestObject) {
        SinglecarePostObject object = new SinglecarePostObject();
        PValue Value = new PValue();
        Value.setNDC(requestObject.getDrugNDC());
        Value.setQuantity(String.valueOf(requestObject.getQuantity()));
        Value.setZipCode(requestObject.getZipcode());
        Value.setDistance(30);
        Value.setMaxResults(30);
        Value.setTenantId(0);
        Value.setNABP("");
        object.setValue(Value);

        try {
            int drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity(), requestObject.getZipcode()).get(0).getId();
            if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 4).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                try {
                    drugRequest.setDrugName(requestObject.getDrugName());
                }catch(Exception ex){

                }
                drugRequest.setProgramId(4);
                drugRequest.setDrugId(drugId+"");
                drugRequest.setNdc(requestObject.getDrugNDC());
                drugRequest.setQuantity(String.valueOf(requestObject.getQuantity()));
                drugRequest.setZipcode(requestObject.getZipcode());
                try {
                    drugRequest.setLatitude(requestObject.getLatitude());
                    drugRequest.setLongitude(requestObject.getLongitude());
                }catch (Exception e){

                }
                drugRequestRepository.save(drugRequest);
            }else{
                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 4).get(0);
                drugRequest.setProgramId(4);
                try {
                    drugRequest.setDrugName(requestObject.getDrugName());
                }catch(Exception ex){

                }
                drugRequest.setDrugId(drugId+"");
                drugRequest.setNdc(requestObject.getDrugNDC());
                drugRequest.setQuantity(String.valueOf(requestObject.getQuantity()));
                drugRequest.setZipcode(requestObject.getZipcode());
                try {
                    drugRequest.setLatitude(requestObject.getLatitude());
                    drugRequest.setLongitude(requestObject.getLongitude());
                }catch (Exception e){

                }
                drugRequestRepository.save(drugRequest);
            }
        } catch (Exception ex) {

        }
        return object;
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<LocatedDrug> getMedImpact(@RequestBody RequestObject requestObject, Map<String, String> longLat, String Brand_indicator) {
        int drugId = 0;
        try {
            drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity(),requestObject.getZipcode()).get(0).getId();

            List<DrugRequest> drugRequests = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 3);

            if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 3).size() != 0) {
                if(drugRequests.get(0).getDrugName() == null || drugRequests.get(0).getDrugName().equals("")){
                    drugRequests.get(0).setDrugName(requestObject.getDrugName());
                    drugRequestRepository.save(drugRequests.get(0));
                }
                MedImpact result = getMedImpactProgramResult(getMedImpactURL(drugRequests.get(0)).intern());
                if (result == null) {
                } else {
                    return CompletableFuture.completedFuture(result.getDrugs().getLocatedDrug().get(0));

                }
            } else {

            }
        } catch (Exception ex) {

        }
        List<LocatedDrugStrength> drugStrengths = null;

        String GSN = "";
        String requestDrug = requestObject.getDrugName().toUpperCase().intern();
        String requestedDosage = requestObject.getDosageStrength().toUpperCase().replaceAll("[A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|%|\\s]", "").trim().intern();

        try {
            requestObject.setGSN(drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity(),requestObject.getZipcode()).get(0).getGsn());
            System.out.println("GSN:"+requestObject.getGSN());
        } catch (Exception ex) {

        }

        if (requestObject.getGSN() != null && !requestObject.getGSN().equals("null")) {
            GSN = requestObject.getGSN();
            MedImpact result = getMedImpactProgramResult(constructMedImpactUrl2(requestObject, longLat, GSN, Brand_indicator).intern());
            if (result == null) {
            } else {
                return CompletableFuture.completedFuture(result.getDrugs().getLocatedDrug().get(0));

            }
        }


        String url = constructMedImpactUrl(requestObject, longLat, Brand_indicator).intern();
        MedImpact medImpact = getMedImpactProgramResult(url);

        if (medImpact != null) {
            if (!CollectionUtils.isEmpty(medImpact.getStrengths().getLocatedDrugStrength())) {
                drugStrengths = medImpact.getStrengths().getLocatedDrugStrength();
                medImpactGSNMap.put(requestDrug, drugStrengths);
            } else {
                return CompletableFuture.completedFuture(medImpactDrug);
            }
        }

        if (!CollectionUtils.isEmpty(drugStrengths)) {
            for (LocatedDrugStrength strength : drugStrengths) {
                String medImpactStrength = strength.getStrength().toUpperCase().replaceAll("[A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|%|\\s]", "").trim().intern();
                if (requestedDosage.equalsIgnoreCase(medImpactStrength)) {
                    GSN = strength.getGsn();
                    break;
                }
            }
        }
        try {
            if (GSN.isEmpty()) {

                CompletableFuture<LocatedDrug> result = CompletableFuture.completedFuture(getMedImpactProgramResult(constructMedImpactUrl2(requestObject, longLat, requestObject.getGSN(), Brand_indicator).intern()).getDrugs().getLocatedDrug().get(0));
                if (result.join() == null || result.join().getPricing().getPrice().equals("")) {

                } else {
                    return result;
                }
            }
        } catch (Exception ex) {

        }
        return (!GSN.isEmpty() && GSN != null)
                ? CompletableFuture.completedFuture(getMedImpactProgramResult(constructMedImpactUrl2(requestObject, longLat, GSN, Brand_indicator).intern()).getDrugs().getLocatedDrug().get(0))
                : CompletableFuture.completedFuture(medImpactDrug);

    }

    private String getMedImpactURL(DrugRequest drugRequest) {
        String s = "https://rxsavings.medimpact.com/web/rxcard/home?p_p_id=com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view" +
                "&p_p_cacheability=cacheLevelPage&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_cmd=get_drug_detail" +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_quantity=" + drugRequest.getQuantity() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_gsn=" + drugRequest.getGsn() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_brandGenericFlag=" + drugRequest.getBrandIndicator() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lat=" + drugRequest.getLatitude() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lng=" +drugRequest.getLongitude() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_numdrugs=1";

        return s;
    }

    private MedImpact getMedImpactProgramResult(String url) {
        WebClient webClient = WebClient.create(url);
        String str = "";
        try {
            str = webClient
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(String.class).block().intern();
        } catch (Exception e) {
            str = "";

        }

        if (!str.equals("")) {


            return gson.fromJson(str, MedImpact.class);
        } else {
            return null;
        }

    }


    private String constructMedImpactUrl(RequestObject requestObject, Map<String, String> latLong, String Brand_indicator) {

        try {
            int drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity(),requestObject.getZipcode()).get(0).getId();
            if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 3).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setProgramId(3);
                drugRequest.setDrugId(drugId+"");
                drugRequest.setDrugName(requestObject.getDrugName());
                drugRequest.setBrandIndicator(Brand_indicator.toUpperCase());
                drugRequest.setLatitude(latLong.get("latitude"));
                drugRequest.setLongitude(latLong.get("longitude"));

                drugRequestRepository.save(drugRequest);
            }else{
                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 3).get(0);
                drugRequest.setProgramId(3);
                drugRequest.setDrugId(drugId+"");
                drugRequest.setDrugName(requestObject.getDrugName());
                drugRequest.setBrandIndicator(Brand_indicator.toUpperCase());
                drugRequest.setLatitude(latLong.get("latitude"));
                drugRequest.setLongitude(latLong.get("longitude"));
                drugRequestRepository.save(drugRequest);
            }
        } catch (Exception ex) {

        }
        return "https://rxsavings.medimpact.com/web/rxcard/home?p_p_id=com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv" +
                "&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability=cacheLevelPage" +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_cmd=get_drug_detail" +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_drugName=" + requestObject.getDrugName() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_brandGenericFlag=" + Brand_indicator.toUpperCase() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lat=" + latLong.get("latitude") +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lng=" + latLong.get("longitude") +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_numdrugs=1";


    }

    private String constructMedImpactUrl2(RequestObject requestObject, Map<String, String> latLong, String gsn, String Brand_indicator) {

        String s = "https://rxsavings.medimpact.com/web/rxcard/home?p_p_id=com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view" +
                "&p_p_cacheability=cacheLevelPage&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_cmd=get_drug_detail" +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_quantity=" + requestObject.getQuantity() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_gsn=" + gsn +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_brandGenericFlag=" + Brand_indicator.toUpperCase() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lat=" + latLong.get("latitude") +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lng=" + latLong.get("longitude") +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_numdrugs=1";
        try {
            int drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity(),requestObject.getZipcode()).get(0).getId();
            if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 3).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setProgramId(3);
                drugRequest.setDrugId(drugId+"");
                drugRequest.setQuantity(requestObject.getQuantity() + "");
                drugRequest.setGsn(gsn);
                drugRequest.setBrandIndicator(Brand_indicator.toUpperCase());
                drugRequest.setLatitude(latLong.get("latitude"));
                drugRequest.setLongitude(latLong.get("longitude"));
                try {
                    drugRequest.setDrugName(requestObject.getDrugName());
                }catch(Exception ex){

                }
                drugRequestRepository.save(drugRequest);
            }else{
               DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 3).get(0);
                drugRequest.setProgramId(3);
                drugRequest.setDrugId(drugId+"");
                drugRequest.setQuantity(requestObject.getQuantity() + "");
                drugRequest.setGsn(gsn);
                drugRequest.setBrandIndicator(Brand_indicator.toUpperCase());
                drugRequest.setLatitude(latLong.get("latitude"));
                drugRequest.setLongitude(latLong.get("longitude"));
                try {
                    drugRequest.setDrugName(requestObject.getDrugName());
                }catch(Exception ex){

                }
                drugRequestRepository.save(drugRequest);
            }
        } catch (Exception ex) {

        }

        return s;
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<InsideRx>> constructInsideRxWebClient(RequestObject requestObject, Map<String, String> longitudeLatitude) throws ExecutionException, InterruptedException {

        PostObject postObject = new PostObject();
        int drugId = 0;
        try {


            drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity(), requestObject.getZipcode()).get(0).getId();

            List<DrugRequest> drugRequests = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 0);

            if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 0).size() != 0) {

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 0).get(0);
                if(drugRequest.getDrugName() == null || drugRequest.getDrugName().equals("")){
                    drugRequest.setDrugName(requestObject.getDrugName());
                    drugRequestRepository.save(drugRequest);
                }
                if(drugRequest.getLatitude()==null){
                    drugRequest.setLatitude(40.585624+"");
                }

                postObject.setLatitude(drugRequest.getLatitude());
                postObject.setLongitude(drugRequest.getLongitude());
                postObject.setNdc(drugRequest.getNdc());
                postObject.setQuantity(drugRequest.getQuantity());
                postObject.setSite_identity("irx");

            } else {
                postObject = getPostObject(requestObject, longitudeLatitude);
            }
        } catch (Exception ex) {
            postObject = getPostObject(requestObject, longitudeLatitude);
        }


        WebClient webClient2 = WebClient.create(INSIDERXURL);
        List<InsideRx> insideRxList = new ArrayList<>();
        try {

            if (drugRequestRepository.findByDrugIdAndProgramId (drugId+"", 0).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setProgramId(0);
                drugRequest.setDrugId(drugId+"");
                drugRequest.setBrandIndicator(requestObject.getDrugType());
                drugRequest.setLongitude(postObject.getLongitude());
                drugRequest.setLatitude(postObject.getLatitude());
                drugRequest.setNdc(postObject.getNdc());
                drugRequest.setQuantity(postObject.getQuantity());
                try {
                    drugRequest.setDrugName(requestObject.getDrugName());
                }catch(Exception ex){

                }
                drugRequestRepository.save(drugRequest);
            }else{

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 0).get(0);
                drugRequest.setProgramId(0);
                drugRequest.setDrugId(drugId+"");
                drugRequest.setBrandIndicator(requestObject.getDrugType());
                drugRequest.setLatitude(postObject.getLatitude());
                drugRequest.setLongitude(postObject.getLongitude());
                drugRequest.setNdc(postObject.getNdc());
                drugRequest.setQuantity(postObject.getQuantity());
                try {
                    drugRequest.setDrugName(requestObject.getDrugName());
                }catch(Exception ex){

                }
                drugRequestRepository.save(drugRequest);
            }
        } catch (Exception ex) {
        }
        try {
            insideRxList = webClient2
                    .post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("csrf-token", CSRF_TOKEN)
                    .header("Cookie", COOKIE)
                    .body(Mono.just(postObject), PostObject.class)
                    .exchange()
                    .flatMapMany(clientResponse -> clientResponse.bodyToFlux(InsideRx.class))
                    .collectList().block();
        } catch (Exception ex) {
        }


        if (!CollectionUtils.isEmpty(insideRxList)) {
            if (!CollectionUtils.isEmpty(insideRxList.get(0).getPrices())) {
                if (insideRxComparator == null)
                    insideRxComparator = constructInsideRxComparator();

                Collections.sort(insideRxList.get(0).getPrices(), insideRxComparator);
            }
        }

        return CompletableFuture.completedFuture(insideRxList);
    }

    private PostObject getPostObject(RequestObject requestObject, Map<String, String> longitudeLatitude) {
        PostObject p = new PostObject();
        p.setLatitude(longitudeLatitude.get("latitude"));
        p.setLongitude(longitudeLatitude.get("longitude"));
        p.setNdc(requestObject.getDrugNDC());
        p.setQuantity(String.valueOf(requestObject.getQuantity()));
        p.setSite_identity("irx");
        return p;
    }

    private Comparator<com.galaxe.drugpriceapi.model.Prices> constructInsideRxComparator() {
        return (o1, o2) -> {
            Double p1 = Double.parseDouble(o1.getPrice());
            Double p2 = Double.parseDouble(o2.getPrice());
            p1.compareTo(p2);
            if (p1 > p2) {
                return 1;
            } else if (p1 < p2) {
                return -1;
            } else {
                return 0;
            }
        };
    }

    private Comparator<Prices> constructSigleCareComparator() {
        return (o1, o2) -> {
            float p1 = Float.parseFloat(o1.getPrice());
            float p2 = Float.parseFloat(o2.getPrice());
            if (p1 > p2) {
                return 1;
            } else if (p1 < p2) {
                return -1;
            } else {
                return 0;
            }
        };

    }


    public CompletableFuture<GoodRxResponse> getGoodRxPrices(RequestObject requestObject) {
        int drugId = 0;
        String str = "";
        DrugRequest drugRequest ;
        List<DrugRequest> drugRequests;
        List<DrugMaster> drugMasters;
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

        try {
            drugMasters =  drugMasterRepository.findAllByFields(requestObject.getDrugNDC(),requestObject.getQuantity(),requestObject.getZipcode());
            drugId =drugMasters.get(0).getId();
            if(drugId == 265659){
                System.out.println("ACYCLOVIR");
            }
            drugRequests = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 6);
            drugRequest =drugRequests.get(0);

            if (drugRequestRepository.findByDrugIdAndProgramId(drugRequest.getDrugId(), 6).size() != 0) {

                WebClient webClient = WebClient.create("https://www.goodrx.com/api/v4/drugs/" + drugRequest.getGood_rx_id() + "/prices?location=" + drugRequest.getLatitude() + "," + drugRequest.getLongitude() + "&location_type=LAT_LNG&distance_mi=6&quantity=" + drugRequest.getQuantity() + "");
                str = webClient
                        .get()
                        .retrieve().bodyToMono(String.class).block();
                GoodRxResponse goodRxResponse = gson.fromJson(str, GoodRxResponse.class);
                try{
                    if(goodRxResponse.getResults().get(0).getPrices().get(0).getPrice() == 0.0){
                        System.out.println("");
                    }
                }catch (Exception ex){
                    System.out.println("Caught");
                }
                return CompletableFuture.completedFuture(goodRxResponse);

            } else {
                return CompletableFuture.completedFuture(new GoodRxResponse());
            }
        }catch(Exception ex){

        }

          return CompletableFuture.completedFuture(new GoodRxResponse());
    }
}
