package com.galaxe.drugpriceapi.web.nap.controller;

import com.galaxe.drugpriceapi.model.DrugNAP2;
import com.galaxe.drugpriceapi.model.Pharmacy;
import com.galaxe.drugpriceapi.model.PriceList;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugMasterRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugMaster;
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

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<Drugs>> getWellRxDrugInfo(RequestObject requestObject, Map<String, String> longitudeLatitude, String brand_indicator) {

        List<Strengths> strengths = null;

        String requestedDrug = requestObject.getDrugName().toUpperCase().intern();

//        if (wellRxDrugGSNMap.containsKey(requestedDrug)) {
//
//            strengths = wellRxDrugGSNMap.get(requestedDrug);
//
//        } else {
            String str = getWellRxOutputString(requestObject, longitudeLatitude).intern();
            WellRx wellRxFirstAPIResp = gson.fromJson(str, WellRx.class);


            if (!CollectionUtils.isEmpty(wellRxFirstAPIResp.getStrengths())) {
                wellRxDrugGSNMap.put(requestedDrug, wellRxFirstAPIResp.getStrengths());
                strengths = wellRxFirstAPIResp.getStrengths();

            } else {
                return CompletableFuture.completedFuture(drugs);
            }
//        }


        if (strengths != null) {

            WellRxSpecifDrugPost obj = new WellRxSpecifDrugPost();
            String dosageStrength = requestObject.getDosageStrength().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%|\\s]", "").trim().intern();

            strengths.forEach(strength -> {
                String dosage = strength.getStrength().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%|\\s]", "").trim().intern();

                if (dosage.equalsIgnoreCase(dosageStrength)) {
                    obj.setGSN(strength.getGSN());
                    return;
                }

            });

            obj.setBgIndicator(brand_indicator);
            obj.setLat(longitudeLatitude.get("latitude"));
            obj.setLng(longitudeLatitude.get("longitude"));
            obj.setNumdrugs("1");
            obj.setQuantity(String.valueOf(requestObject.getQuantity()));
            obj.setBReference(requestObject.getDrugName());
            obj.setNcpdps("null");

            if (obj.getGSN() != null && !obj.getGSN().isEmpty()) {
                String wellRxSpecificDrugResponseStr = getWellRxDrugSpecificOutput(obj).intern();
                List<Drugs> wellRxSpecificDrugs = gson.fromJson(wellRxSpecificDrugResponseStr, WellRx.class).getDrugs();

                if (!CollectionUtils.isEmpty(wellRxSpecificDrugs)) {
                    if (constructWellRxComparator == null)
                        constructWellRxComparator = constructWellRxComparator();

                    Collections.sort(wellRxSpecificDrugs, constructWellRxComparator);

                    return CompletableFuture.completedFuture(wellRxSpecificDrugs);
                }
            }
        }
        if(drugs.size() == 0){
           // wellRxFirstAPIResp
            requestObject.setGSN(wellRxFirstAPIResp.getForms().get(1).getGSN());
            String str2 = getWellRxOutputString2(requestObject, longitudeLatitude, brand_indicator).intern();

            WellRx wellRxFirstAPIResp2 = gson.fromJson(str2, WellRx.class);
            System.out.println("HERE");
            CompletableFuture<List<Drugs>> result = getSpecWellRxDrug(wellRxFirstAPIResp2,requestedDrug,new ArrayList<>(), requestObject,longitudeLatitude, brand_indicator);
            if(result.join().size() != 0){
                try {
                    DrugMaster drugMaster = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0);
                    drugMaster.setGsn(result.join().get(0).getGSN());
                    drugMasterRepository.save(drugMaster);
                }catch(Exception ex){

                }
                return result;
            }
        }
        return CompletableFuture.completedFuture(drugs);
    }
    public  CompletableFuture<List<Drugs>> getSpecWellRxDrug(WellRx wellRxFirstAPIResp, String requestedDrug,List<Strengths> strengths, RequestObject requestObject, Map<String, String> longitudeLatitude, String brand_indicator){

        if (!CollectionUtils.isEmpty(wellRxFirstAPIResp.getStrengths())) {
            wellRxDrugGSNMap.put(requestedDrug, wellRxFirstAPIResp.getStrengths());
            strengths = wellRxFirstAPIResp.getStrengths();
        } else {
            return CompletableFuture.completedFuture(drugs);
        }
//        }


        if (strengths != null) {

            WellRxSpecifDrugPost obj = new WellRxSpecifDrugPost();
            String dosageStrength = requestObject.getDosageStrength().toUpperCase().replaceAll("[MG|MCG|ML|(VA)|MG-MCG|%|\\s]", "").trim().intern();

            strengths.forEach(strength -> {
                String dosage = strength.getStrength().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%|\\s]", "").trim().intern();

                if (dosage.equalsIgnoreCase(dosageStrength)) {
                    obj.setGSN(strength.getGSN());
                    return;
                }

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
        if(drugType.equals("B")){
            drugType = "BRAND_WITH_GENERIC";
        }else if (drugType.equals("G")){
            drugType = "GENERIC";
        }

        WebClient webClient = WebClient.create("https://api.uspharmacycard.com/drug/price/147/020982/" + requestObject.getZipcode()
                + "/" + requestObject.getDrugNDC() + "/" + requestObject.getDrugName() + "/" + drugType + "/" + requestObject.getQuantity() + "/10");

        List<DrugNAP2> usPharmacies = webClient
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(DrugNAP2.class))
                .collectList().block();


        if (!CollectionUtils.isEmpty(usPharmacies.get(0).getPriceList())) {
            List<PriceList> priceList = usPharmacies.get(0).getPriceList();



            if (constructUsDrugComparator == null)
                constructUsDrugComparator = constructUsDrugComparator();

            Collections.sort(priceList, constructUsDrugComparator);
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
    String getWellRxOutputString2(RequestObject requestObject, Map<String, String> longitudeLatitude, String brand_indicator)  {
//        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-specific-drug");
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-specific-drug");

        WellRxGSNSearch wellRxGSNSearch = new WellRxGSNSearch();
        wellRxGSNSearch.setGSN(requestObject.getGSN());
        wellRxGSNSearch.setLat(longitudeLatitude.get("latitude"));
        wellRxGSNSearch.setLng(longitudeLatitude.get("longitude"));
        wellRxGSNSearch.setNumdrugs("1");
        wellRxGSNSearch.setQuantity(requestObject.getQuantity()+"");
        wellRxGSNSearch.setBgIndicator(brand_indicator);
        wellRxGSNSearch.setbReference(requestObject.getDrugName());
        wellRxGSNSearch.setNcpdps("null");

         Mono<String> s = webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Host","www.wellrx.com")
                .header("Referer", "https://www.wellrx.com/prescriptions/humatrope/somerset")
                .header("Cookie", COOKIE_WELLRX2)
                .header("X-Requested-With", "XMLHttpRequest")
                .body(Mono.just(wellRxGSNSearch), WellRxGSNSearch.class)
                .retrieve().bodyToMono(String.class);

        return s.block() ;

    }

    private String getWellRxDrugSpecificOutput(WellRxSpecifDrugPost wellRxSpecifDrugPost) {
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-drugs");
        try{
            System.out.println("SPECIFIC DRUG");
            System.out.println(wellRxSpecifDrugPost.getGSN());
        }catch(Exception ex){

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
                    .header("Host","www.wellrx.com")
                    .header("Referer", "https://www.wellrx.com/prescriptions/humatrope/somerset")
                    .header("Cookie", COOKIE_WELLRX2)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body(Mono.just(wellRxGSNSearch), WellRxGSNSearch.class)
                    .retrieve().bodyToMono(String.class);

            return s.block() ;

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
