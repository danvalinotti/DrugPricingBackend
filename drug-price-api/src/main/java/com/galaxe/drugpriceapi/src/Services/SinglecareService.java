package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.SinglecareRequest.PValue;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.SinglecareRequest.SingleCareRequest;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.SinglecareResponse.PharmacyPricings;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.SinglecareResponse.Prices;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.SinglecareResponse.SinglecareResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
@Component
public class SinglecareService {
    @Autowired
    DrugRequestRepository drugRequestRepository;
    @Autowired
    DrugMasterRepository drugMasterRepository;

    private Gson gson = new Gson();
    private final String SINGLECAREURL = "https://api.singlecare.com/Services/v1_0/Public/PBMService.svc/GetTieredPricing";
    private Comparator<Prices> constructSinglecareComparator = null;

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<PharmacyPricings> getSinglecarePrices(@RequestBody UIRequestObject UIRequestObject) {

        int drugId = 0;
        String str = "";
        try {
            drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();

            List<DrugRequest> drugRequests = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 4);

            if (drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 4).size() != 0) {
                String json = gson.toJson(constructSinglecarePostObject(drugRequests.get(0)));
//                System.out.println("DrugSaved");
                if (drugRequests.get(0).getDrugName() == null || drugRequests.get(0).getDrugName().equals("")) {
                    drugRequests.get(0).setDrugName(UIRequestObject.getDrugName());
                    drugRequestRepository.save(drugRequests.get(0));
                }

                WebClient webClient = WebClient.create(SINGLECAREURL);
                str = webClient
                        .post()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(Mono.just(json), String.class)
                        .retrieve().bodyToMono(String.class).block();
                SinglecareResponse singlecareResponse = gson.fromJson(str, SinglecareResponse.class);

                if (singlecareResponse.getValue() != null) {
                    if (!CollectionUtils.isEmpty(singlecareResponse.getValue().getPharmacyPricings())) {

                        if (constructSinglecareComparator == null)
                            constructSinglecareComparator = constructSigleCareComparator();

                        Collections.sort(singlecareResponse.getValue().getPharmacyPricings().get(0).getPrices(), constructSinglecareComparator);
                        return CompletableFuture.completedFuture((singlecareResponse.getValue().getPharmacyPricings().get(0)));
                    }
                }
            } else {
                String json = gson.toJson(constructSinglecarePostObject(UIRequestObject));

                WebClient webClient = WebClient.create(SINGLECAREURL);
                str = webClient
                        .post()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(Mono.just(json), String.class)
                        .retrieve().bodyToMono(String.class).block();
                SinglecareResponse singlecareResponse = gson.fromJson(str, SinglecareResponse.class);
                try {
//                    System.out.println("SINGLECARE");
//                    System.out.println(singlecare.getValue().getPharmacyPricings().get(0).getPrices().get(0));
                } catch (Exception ex) {

                }
                if (singlecareResponse.getValue() != null) {
                    if (!CollectionUtils.isEmpty(singlecareResponse.getValue().getPharmacyPricings())) {

                        if (constructSinglecareComparator == null)
                            constructSinglecareComparator = constructSigleCareComparator();

                        Collections.sort(singlecareResponse.getValue().getPharmacyPricings().get(0).getPrices(), constructSinglecareComparator);
                        return CompletableFuture.completedFuture((singlecareResponse.getValue().getPharmacyPricings().get(0)));
                    }
                }
            }
        } catch (Exception ex) {
            String json = gson.toJson(constructSinglecarePostObject(UIRequestObject));

            WebClient webClient = WebClient.create(SINGLECAREURL);
            str = webClient
                    .post()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(Mono.just(json), String.class)
                    .retrieve().bodyToMono(String.class).block();
            SinglecareResponse singlecareResponse = gson.fromJson(str, SinglecareResponse.class);
            try {
//                System.out.println("SINGLECARE");
//                System.out.println(singlecare.getValue().getPharmacyPricings().get(0).getPrices().get(0));
            } catch (Exception e) {

            }
            if (singlecareResponse.getValue() != null) {
                if (!CollectionUtils.isEmpty(singlecareResponse.getValue().getPharmacyPricings())) {

                    if (constructSinglecareComparator == null)
                        constructSinglecareComparator = constructSigleCareComparator();

                    Collections.sort(singlecareResponse.getValue().getPharmacyPricings().get(0).getPrices(), constructSinglecareComparator);
                    return CompletableFuture.completedFuture((singlecareResponse.getValue().getPharmacyPricings().get(0)));
                }
            }
        }
        String json = gson.toJson(constructSinglecarePostObject(UIRequestObject));

        WebClient webClient = WebClient.create(SINGLECAREURL);
        str = webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(json), String.class)
                .retrieve().bodyToMono(String.class).block();
        SinglecareResponse singlecareResponse = gson.fromJson(str, SinglecareResponse.class);
        try {
//            System.out.println("SINGLECARE");
//            System.out.println(singlecare.getValue().getPharmacyPricings().get(0).getPrices().get(0));
        } catch (Exception e) {

        }
        if (singlecareResponse.getValue() != null) {
            if (!CollectionUtils.isEmpty(singlecareResponse.getValue().getPharmacyPricings())) {

                if (constructSinglecareComparator == null)
                    constructSinglecareComparator = constructSigleCareComparator();

                Collections.sort(singlecareResponse.getValue().getPharmacyPricings().get(0).getPrices(), constructSinglecareComparator);
                return CompletableFuture.completedFuture((singlecareResponse.getValue().getPharmacyPricings().get(0)));
            }
        }


//        System.out.println("null");
        return CompletableFuture.completedFuture(new PharmacyPricings());
    }

    private SingleCareRequest constructSinglecarePostObject(DrugRequest drugRequest) {
        SingleCareRequest object = new SingleCareRequest();
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


    private SingleCareRequest constructSinglecarePostObject(UIRequestObject UIRequestObject) {
        SingleCareRequest object = new SingleCareRequest();
        PValue Value = new PValue();
        Value.setNDC(UIRequestObject.getDrugNDC());
        Value.setQuantity(String.valueOf(UIRequestObject.getQuantity()));
        Value.setZipCode(UIRequestObject.getZipcode());
        Value.setDistance(30);
        Value.setMaxResults(30);
        Value.setTenantId(0);
        Value.setNABP("");
        object.setValue(Value);

        try {
            int drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();
            if (drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 4).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                try {
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                } catch (Exception ex) {

                }
                drugRequest.setProgramId(4);
                drugRequest.setDrugId(drugId + "");
                drugRequest.setNdc(UIRequestObject.getDrugNDC());
                drugRequest.setQuantity(String.valueOf(UIRequestObject.getQuantity()));
                drugRequest.setZipcode(UIRequestObject.getZipcode());
                try {
                    drugRequest.setLatitude(UIRequestObject.getLatitude());
                    drugRequest.setLongitude(UIRequestObject.getLongitude());
                } catch (Exception e) {

                }
                drugRequestRepository.save(drugRequest);
            } else {
                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 4).get(0);
                drugRequest.setProgramId(4);
                try {
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                } catch (Exception ex) {

                }
                drugRequest.setDrugId(drugId + "");
                drugRequest.setNdc(UIRequestObject.getDrugNDC());
                drugRequest.setQuantity(String.valueOf(UIRequestObject.getQuantity()));
                drugRequest.setZipcode(UIRequestObject.getZipcode());
                try {
                    drugRequest.setLatitude(UIRequestObject.getLatitude());
                    drugRequest.setLongitude(UIRequestObject.getLongitude());
                } catch (Exception e) {

                }
                drugRequestRepository.save(drugRequest);
            }
        } catch (Exception ex) {

        }
        return object;
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

}
