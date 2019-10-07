package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse.LocatedDrug;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse.LocatedDrugStrength;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse.MedImpact;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
@Component
public class MedImpactService {

    @Autowired
    DrugRequestRepository drugRequestRepository;
    @Autowired
    DrugMasterRepository drugMasterRepository;

    private Gson gson = new Gson();
    private LocatedDrug medImpactDrug = new LocatedDrug();
    private Map<String, List<LocatedDrugStrength>> medImpactGSNMap = new HashMap<>();

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

    private String getMedImpactURL(DrugRequest drugRequest) {
        String s = "https://rxsavings.Medimpact.com/web/rxcard/home?p_p_id=com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view" +
                "&p_p_cacheability=cacheLevelPage&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_cmd=get_drug_detail" +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_quantity=" + drugRequest.getQuantity() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_gsn=" + drugRequest.getGsn() +
                "&_com_cashcard_portal_portlet_CashCardPortlet_INSTANCE_wVwgc3hAI7xv_brandGenericFlag=" + drugRequest.getBrandIndicator() +
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
