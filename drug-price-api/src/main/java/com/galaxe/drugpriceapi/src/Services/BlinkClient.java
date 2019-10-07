package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.BlinkResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.PriceResponse.*;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.PharmacyResponse.BlinkPharmacyResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.PharmacyResponse.PharmacyDetails;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class BlinkClient {

    private Gson gson = new Gson();

    private PharmacyDetails pharmacyDetails = new PharmacyDetails();

    private Price price = new Price();

    @Autowired
    DrugRequestRepository drugRequestRepository;

    @Autowired
    DrugMasterRepository drugMasterRepository;

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<PharmacyDetails> getBlinkPharmacy(UIRequestObject UIRequestObject) {
        String url = constructBlinkPharmacyURL(UIRequestObject.getZipcode()).intern();
        WebClient webClient = WebClient.create(url);
        String str = webClient
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class).block();
        BlinkPharmacyResponse pharmacy = gson.fromJson(str, BlinkPharmacyResponse.class);
        for (PharmacyDetails r : pharmacy.getResult().getResults()) {
            if (r.getIs_supersaver().equalsIgnoreCase("true")) {
                return CompletableFuture.completedFuture(r);
            }
        }
        return CompletableFuture.completedFuture(pharmacyDetails);

    }


    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Price> getBlinkPrice(UIRequestObject UIRequestObject) {
       String url = constructBlinkPriceURL(UIRequestObject.getDrugName()).intern();
       String requestedDosage = UIRequestObject.getDosageStrength().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%]", "").trim().intern();
       String str = "";

        try {
            WebClient webClient = WebClient.create(url);
             str = webClient
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(String.class).block();
        }catch(Exception e){
            return CompletableFuture.completedFuture(new Price());
        }
        BlinkPriceResponse blinkPriceResponse = gson.fromJson(str, BlinkPriceResponse.class);

        if(blinkPriceResponse.getResult().getDrug().getFormatted_name().contains("Generic")){
            return CompletableFuture.completedFuture(new Price());
        }

        if (!CollectionUtils.isEmpty(blinkPriceResponse.getResult().getDrug().getForms())) {
            for (Forms form : blinkPriceResponse.getResult().getDrug().getForms()) {


            if (!CollectionUtils.isEmpty(form.getDosages())) {
                for (Dosages dosage : form.getDosages()) {
                    String blinkDosage = dosage.getDisplay_dosage().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%]", "").trim().intern();
                    if (blinkDosage.equalsIgnoreCase(requestedDosage)) {
                        for (Quantities q : dosage.getQuantities()) {
                            Double d = Double.parseDouble(String.valueOf(UIRequestObject.getQuantity()));
                            if (q.getRaw_quantity().equalsIgnoreCase(String.valueOf(d))) {
                                Price p = q.getPrice();
                                p.setMedId(dosage.getMed_id());
                                return CompletableFuture.completedFuture(p);
                            }
                        }

                    }
                }
            }
            }
        }
        return CompletableFuture.completedFuture(new Price());

    }


    private String constructBlinkPharmacyURL(String zipcode) {
        return "https://www.blinkhealth.com/api/v2/pharmacies?limit=10&allow_out_of_network=false&zip_code=" + zipcode + "&c_app=rx&c_platform=web&c_timestamp=1557344265151";
    }

    private String constructBlinkPriceURL(String name) {

        String newName = name.replace(" ", "-");
        return "https://www.blinkhealth.com/api/v2/user/drugs/detail/" +newName + "?c_app=rx&c_platform=web&c_timestamp=1557342444013";
    }
    @Async
    public CompletableFuture<BlinkResponse> getBlinkPharmacyPrice(UIRequestObject UIRequestObject) throws ExecutionException, InterruptedException {
        try {
            CompletableFuture<PharmacyDetails> pharmacy = getBlinkPharmacy(UIRequestObject);

            CompletableFuture<Price> price = getBlinkPrice(UIRequestObject);

            if(UIRequestObject.getDrugName().equals("Sildenafil Citrate")){
                System.out.println("GLIME PRICE");
            }
            if(price.isDone() == false){
                UIRequestObject.setDrugName(UIRequestObject.getDrugName().split("\\s")[0]);
                price = getBlinkPrice(UIRequestObject);
            }
            //Wait until they are all done
            CompletableFuture.allOf(pharmacy, price).join();
            Local lowestPrice = getLowestPrice(price.join());
            try {
                if(price.join().getLocal().getRaw_value()!="") {
                    int drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();
                    try {
                        int size = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 5).size();
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 5).size() == 0) {
                        DrugRequest drugRequest = new DrugRequest();
                        drugRequest.setZipcode(UIRequestObject.getZipcode());
                        drugRequest.setDrugName(UIRequestObject.getDrugName().replace(" ", "-"));
                        drugRequest.setProgramId(5);
                        drugRequest.setGsn(price.join().getMedId());
                        drugRequest.setDrugId(drugId+"");
                        drugRequestRepository.save(drugRequest);
                    } else {
                        DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 5).get(0);
                        drugRequest.setZipcode(UIRequestObject.getZipcode());
                        drugRequest.setDrugName(UIRequestObject.getDrugName().replace(" ", "-"));
                        drugRequest.setProgramId(5);
                        drugRequest.setDrugId(drugId+"");
                        drugRequestRepository.save(drugRequest);
                    }
                }
            } catch (Exception ex) {

            }
            if (pharmacy != null && price != null) {
                BlinkResponse blinkResponse = new BlinkResponse();
                blinkResponse.setPrice(price.get());
                blinkResponse.setPharmacyDetails(pharmacy.get());
                return CompletableFuture.completedFuture(blinkResponse);
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new BlinkResponse());
        }
        return CompletableFuture.completedFuture(new BlinkResponse());
    }

    private Local getLowestPrice(Price price) {
        List<Local> prices = new ArrayList<>();
        if(price != null) {
            try {
                prices.add(price.getLocal());
            } catch (Exception e) {
                e.printStackTrace();

            }
            try {
                prices.add(price.getDelivery());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                prices.add(price.getEdlp());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                prices.add(price.getRetail());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Local lowestLocal = new Local();
            for (int i = 0; i < prices.size(); i++) {
                if (lowestLocal == null) {
                    lowestLocal = prices.get(i);
                } else {
                    try {

                        if (Integer.parseInt(lowestLocal.getRaw_value()) > Integer.parseInt(prices.get(i).getRaw_value())) {
                            lowestLocal = prices.get(i);
                        }
                    } catch (Exception e) {
//                    e.printStackTrace();
                    }
                }
            }
            return lowestLocal;
        }
        return new Local();
    }
}
