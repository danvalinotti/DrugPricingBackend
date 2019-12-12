package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse.LocatedDrug;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse.LocatedDrugStrength;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse.MedImpact;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.galaxe.drugpriceapi.src.Services.KrogerPriceService.isKroger;

@Component
public class MedImpactService {

    @Autowired
    DrugRequestRepository drugRequestRepository;
    @Autowired
    DrugMasterRepository drugMasterRepository;

    private Gson gson = new Gson();
    private LocatedDrug medImpactDrug = new LocatedDrug();
    private Map<String, List<LocatedDrugStrength>> medImpactGSNMap = new HashMap<>();

    static ArrayList<Price> getMedImpactPrices(DrugRequest drugRequest) {
        try {
            // Build URL and create WebClient
            String url = getMedImpactURL(drugRequest);
            WebClient webClient = WebClient.create(url);
            // Make HTTP POST request to API
            Mono<String> s = webClient
                    .post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(String.class);
            String block = s.block();

            // Parse JSON response
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(Objects.requireNonNull(block));
            ArrayList<Price> pricesByRank = new ArrayList<>(5);

            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                pricesByRank.add(null);
                pricesByRank.add(null);
                pricesByRank.add(null);
                pricesByRank.add(null);
                pricesByRank.add(null);
                ArrayList<Double> lowestPrices = new ArrayList<>(5);
                lowestPrices.add(0.0);
                lowestPrices.add(0.0);
                lowestPrices.add(0.0);
                lowestPrices.add(0.0);
                lowestPrices.add(0.0);
                ArrayList<Price> otherPrices = new ArrayList<>();

                if (jsonObject != null) {
                    // Extract JSON array of prices
                    JsonArray prices = jsonObject.get("drugs").getAsJsonObject().get("locatedDrug").getAsJsonArray();
                    // Loop through prices in response
                    for (JsonElement price : prices) {
                        JsonObject priceObject = price.getAsJsonObject();
//                        LinkedHashMap priceMap = (LinkedHashMap) price;
                        Price p = new Price();
                        p.setProgramId(2);
                        p.setPharmacy(priceObject.get("pharmacy").getAsJsonObject().get("name").getAsString());
                        p.setPrice(priceObject.get("pricing").getAsJsonObject().get("price").getAsDouble());
                        p.setUncPrice(null);

                        if (p.getPharmacy().toUpperCase().contains("CVS")) {
                            System.out.println("CVS PRICE: " + p.getPrice());
                            if (pricesByRank.get(0) == null || lowestPrices.get(0) > p.getPrice()) {
                                p.setRank(0);
                                pricesByRank.set(0, p);
                                lowestPrices.set(0, p.getPrice());
                            }
                        } else if (p.getPharmacy().toUpperCase().contains("WALMART")) {
                            System.out.println("WAL-MART PRICE: " + p.getPrice());
                            if (pricesByRank.get(1) == null || lowestPrices.get(1) > p.getPrice()) {
                                p.setRank(1);
                                pricesByRank.set(1, p);
                                lowestPrices.set(1, p.getPrice());
                            }
                        } else if (p.getPharmacy().toUpperCase().contains("WALGREENS")) {
                            System.out.println("WALGREENS PRICE: " + p.getPrice());
                            if (pricesByRank.get(2) == null || lowestPrices.get(2) > p.getPrice()) {
                                p.setRank(2);
                                pricesByRank.set(2, p);
                                lowestPrices.set(2, p.getPrice());
                            }
                        } else if (isKroger(p.getPharmacy().toUpperCase())) {
                            System.out.println("KROGER PRICE: " + p.getPrice());
                            if (pricesByRank.get(3) == null || lowestPrices.get(3) > p.getPrice()) {
                                p.setRank(3);
                                pricesByRank.set(3, p);
                                lowestPrices.set(3, p.getPrice());
                            }
                        } else {
                            System.out.println("FOUND OTHER PRICE: " + p.getPrice());
                            otherPrices.add(p);
                            if (pricesByRank.get(4) == null || lowestPrices.get(4) > p.getPrice()) {
                                p.setRank(4);
                                pricesByRank.set(4, p);
                                lowestPrices.set(4, p.getPrice());
                            }
                        }
                    }

                    while (pricesByRank.indexOf(null) != -1 && otherPrices.size() > 0) {
                        pricesByRank.set(pricesByRank.indexOf(null), otherPrices.get(0));
                        otherPrices.remove(0);
                    }
                }
            }

            return pricesByRank;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<LocatedDrug> getMedImpact(@RequestBody UIRequestObject UIRequestObject, Map<String, String> longLat, String Brand_indicator) {
        int drugId = 0;
        try {
            drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();

            List<DrugRequest> drugRequests = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 3);

            if (drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 3).size() != 0) {
                if (drugRequests.get(0).getDrugName() == null || drugRequests.get(0).getDrugName().equals("")) {
                    drugRequests.get(0).setDrugName(UIRequestObject.getDrugName());
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
        String requestDrug = UIRequestObject.getDrugName().toUpperCase().intern();
        String requestedDosage = UIRequestObject.getDosageStrength().toUpperCase().replaceAll("[A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|%|\\s]", "").trim().intern();

        try {
            UIRequestObject.setGSN(drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getGsn());
            System.out.println("GSN:" + UIRequestObject.getGSN());
        } catch (Exception ex) {

        }

        if (UIRequestObject.getGSN() != null && !UIRequestObject.getGSN().equals("null")) {
            GSN = UIRequestObject.getGSN();
            MedImpact result = getMedImpactProgramResult(constructMedImpactUrl2(UIRequestObject, longLat, GSN, Brand_indicator).intern());
            if (result == null) {
            } else {
                return CompletableFuture.completedFuture(result.getDrugs().getLocatedDrug().get(0));

            }
        }


        String url = constructMedImpactUrl(UIRequestObject, longLat, Brand_indicator).intern();
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

                CompletableFuture<LocatedDrug> result = CompletableFuture.completedFuture(getMedImpactProgramResult(constructMedImpactUrl2(UIRequestObject, longLat, UIRequestObject.getGSN(), Brand_indicator).intern()).getDrugs().getLocatedDrug().get(0));
                if (result.join() == null || result.join().getPricing().getPrice().equals("")) {

                } else {
                    return result;
                }
            }
        } catch (Exception ex) {

        }
        return (!GSN.isEmpty() && GSN != null)
                ? CompletableFuture.completedFuture(getMedImpactProgramResult(constructMedImpactUrl2(UIRequestObject, longLat, GSN, Brand_indicator).intern()).getDrugs().getLocatedDrug().get(0))
                : CompletableFuture.completedFuture(medImpactDrug);

    }

    private static String getMedImpactURL(DrugRequest drugRequest) {
        String s = "https://rxsavings.Medimpact.com/web/rxcard/home?p_p_id=com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view" +
                "&p_p_cacheability=cacheLevelPage&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_cmd=get_drug_detail" +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_quantity=" + drugRequest.getQuantity() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_drugName=" + drugRequest.getDrugName() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_brandGenericFlag=" + drugRequest.getBrandIndicator().charAt(0) +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lat=" + drugRequest.getLatitude() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lng=" + drugRequest.getLongitude() +
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


    private String constructMedImpactUrl(UIRequestObject UIRequestObject, Map<String, String> latLong, String Brand_indicator) {

        try {
            int drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();
            if (drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 3).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setProgramId(3);
                drugRequest.setDrugId(drugId + "");
                drugRequest.setDrugName(UIRequestObject.getDrugName());
                drugRequest.setBrandIndicator(Brand_indicator.toUpperCase());
                drugRequest.setLatitude(latLong.get("latitude"));
                drugRequest.setLongitude(latLong.get("longitude"));

                drugRequestRepository.save(drugRequest);
            } else {
                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 3).get(0);
                drugRequest.setProgramId(3);
                drugRequest.setDrugId(drugId + "");
                drugRequest.setDrugName(UIRequestObject.getDrugName());
                drugRequest.setBrandIndicator(Brand_indicator.toUpperCase());
                drugRequest.setLatitude(latLong.get("latitude"));
                drugRequest.setLongitude(latLong.get("longitude"));
                drugRequestRepository.save(drugRequest);
            }
        } catch (Exception ex) {

        }
        return "https://rxsavings.Medimpact.com/web/rxcard/home?p_p_id=com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv" +
                "&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability=cacheLevelPage" +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_cmd=get_drug_detail" +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_drugName=" + UIRequestObject.getDrugName() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_brandGenericFlag=" + Brand_indicator.toUpperCase() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lat=" + latLong.get("latitude") +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lng=" + latLong.get("longitude") +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_numdrugs=1";


    }

    private String constructMedImpactUrl2(UIRequestObject UIRequestObject, Map<String, String> latLong, String gsn, String Brand_indicator) {

        String s = "https://rxsavings.Medimpact.com/web/rxcard/home?p_p_id=com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view" +
                "&p_p_cacheability=cacheLevelPage&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_cmd=get_drug_detail" +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_quantity=" + UIRequestObject.getQuantity() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_gsn=" + gsn +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_brandGenericFlag=" + Brand_indicator.toUpperCase() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lat=" + latLong.get("latitude") +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_lng=" + latLong.get("longitude") +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_numdrugs=1";
        try {
            int drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();
            if (drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 3).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setProgramId(3);
                drugRequest.setDrugId(drugId + "");
                drugRequest.setQuantity(UIRequestObject.getQuantity() + "");
                drugRequest.setGsn(gsn);
                drugRequest.setBrandIndicator(Brand_indicator.toUpperCase());
                drugRequest.setLatitude(latLong.get("latitude"));
                drugRequest.setLongitude(latLong.get("longitude"));
                try {
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                } catch (Exception ex) {

                }
                drugRequestRepository.save(drugRequest);
            } else {
                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 3).get(0);
                drugRequest.setProgramId(3);
                drugRequest.setDrugId(drugId + "");
                drugRequest.setQuantity(UIRequestObject.getQuantity() + "");
                drugRequest.setGsn(gsn);
                drugRequest.setBrandIndicator(Brand_indicator.toUpperCase());
                drugRequest.setLatitude(latLong.get("latitude"));
                drugRequest.setLongitude(latLong.get("longitude"));
                try {
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                } catch (Exception ex) {

                }
                drugRequestRepository.save(drugRequest);
            }
        } catch (Exception ex) {

        }

        return s;
    }
}
