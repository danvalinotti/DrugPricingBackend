package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.InsideRxRequest.InsideRxRequest;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.InsideRxResponse.InsideRxPrice;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.InsideRxResponse.InsideRxResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.PriceDetailsComparator;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.CollectionUtils;
import reactor.util.LinkedMultiValueMap;
import reactor.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.galaxe.drugpriceapi.src.Services.KrogerPriceService.isKroger;
import static java.lang.Double.parseDouble;

@Component
public class InsideRxService {

    private final String CSRF_TOKEN = "Hi6yGXfg-vppErZsd2KXvKmH9LxjPBNJeK48";
    private final String COOKIE = "_gcl_au=1.1.1639244140.1555443999; _fbp=fb.1.1555443999440.65320427; _ga=GA1.2.711100002.1555444000; _gid=GA1.2.317294123.1555444000; _hjIncludedInSample=1; _csrf=Z3iefjYKIjIUIEXBJgTix0BY; _gat_UA-113293481-1=1; geocoords=40.7473758%2C-74.05057520000003; AWSALB=6NBPPYHYpRwHG5ONO7yvFP6fzmSCfiDRLUr3FCKprscG4ld2CKg2lU+ZRCxhxrTF55clcMF7APSLyeZBhLeH2pv/9pzCIWt8u9lcfJfF8La8Z/eIpABRoF3orpJj";
    private Comparator<InsideRxPrice> insideRxComparator = null;
    private final String INSIDERXURL = "https://insiderx.com/request/pharmacies";
    private Gson gson = new Gson();

    @Autowired
    DrugRequestRepository drugRequestRepository;
    @Autowired
    DrugMasterRepository drugMasterRepository;

    public static ArrayList<Price> getInsideRxPrice(DrugRequest drugRequest) {
        String url = "https://insiderx.com/request/pharmacies";
        RestTemplate template = new RestTemplate();

        // Set POST Headers
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.setAccept(mediaTypes);
        headers.set("Cookie", "_gcl_au=1.1.923916117.1571676777; _fbp=fb.1.1571676776869.2055137922; _ga=GA1.2.930772864.1571676778; _gid=GA1.2.1882699394.1571676778; _gat_UA-113293481-1=1; _hjid=d6e7565b-1525-4271-8198-042e450e45ac; _hjIncludedInSample=1; geocoords=40.7350747%2C-74.17390569999998; AWSALB=mSNItEQ6fXmxXUsxt5mlUriIXhzEHmbChrsjCmQCdVdp42tXWv07gpOMfIQjeOlkAmbeYVCzgbur0wS6jc3a92h9ZKJJb9cNCF7qpmn5FKV9PH3VfDW/CsYPWDt2");
        headers.set("X-Requested-With", "XMLHttpRequest");
        headers.set("csrf-token", "Hi6yGXfg-vppErZsd2KXvKmH9LxjPBNJeK48");

        // Set REST request body
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("ndc", drugRequest.getNdc());
        map.add("latitude", drugRequest.getLatitude());
        map.add("longitude", drugRequest.getLongitude());
        map.add("quantity", drugRequest.getQuantity());
        map.add("referrer", "null");
        map.add("site_identity", "irx");

        try {
            // Make POST request
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<Object> response = template.postForEntity(url, request, Object.class);

            // Build response object
            Object responseBody = response.getBody();
            LinkedHashMap linkedHashMap = (LinkedHashMap) responseBody;
            ArrayList<Price> pricesByRank = new ArrayList<>(5);
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

            if (linkedHashMap != null) {
                ArrayList prices = (ArrayList) linkedHashMap.get("prices");

                // Loop through prices in response
                for (Object price : prices) {
                    LinkedHashMap priceMap = (LinkedHashMap) price;
                    LinkedHashMap pharmacy = (LinkedHashMap) priceMap.get("pharmacy");
                    Price p = new Price();
                    p.setProgramId(0);
                    p.setPharmacy((String) pharmacy.get("name"));
                    p.setPrice(parseDouble(((String) priceMap.get("price"))));
                    if (priceMap.get("uncPrice") != null) {
                        p.setUncPrice(parseDouble((String) priceMap.get("uncPrice")));
                    } else {
                        p.setUncPrice(null);
                    }

                    if (p.getPharmacy().toUpperCase().contains("CVS")) {
                        System.out.println("CVS PRICE: " + p.getPrice());
                        if (pricesByRank.get(0) == null || lowestPrices.get(0) > p.getPrice()) {
                            p.setRank(0);
                            pricesByRank.set(0, p);
                            lowestPrices.set(0, p.getPrice());
                        }
                    } else if (p.getPharmacy().toUpperCase().contains("WAL-MART")) {
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

                while (pricesByRank.indexOf(null) != -1) {
                    if (otherPrices.size() > 0 ) {
                        pricesByRank.set(pricesByRank.indexOf(null), otherPrices.get(0));
                        otherPrices.remove(0);
                    } else {
                        pricesByRank.remove(pricesByRank.indexOf(null));
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
    public CompletableFuture<List<InsideRxResponse>> constructInsideRxWebClient(UIRequestObject UIRequestObject, Map<String, String> longitudeLatitude) throws ExecutionException, InterruptedException {

        InsideRxRequest insideRxRequest = new InsideRxRequest();
        int drugId = 0;
        try {


            drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();

            List<DrugRequest> drugRequests = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 0);

            if (drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 0).size() != 0) {

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 0).get(0);
                if (drugRequest.getDrugName() == null || drugRequest.getDrugName().equals("")) {
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                    drugRequestRepository.save(drugRequest);
                }
                if (drugRequest.getLatitude() == null) {
                    drugRequest.setLatitude(40.585624 + "");
                }

                insideRxRequest.setLatitude(drugRequest.getLatitude());
                insideRxRequest.setLongitude(drugRequest.getLongitude());
                insideRxRequest.setNdc(drugRequest.getNdc());
                insideRxRequest.setQuantity(drugRequest.getQuantity());
                insideRxRequest.setSite_identity("irx");

            } else {
                insideRxRequest = getPostObject(UIRequestObject, longitudeLatitude);
            }
        } catch (Exception ex) {
            insideRxRequest = getPostObject(UIRequestObject, longitudeLatitude);
        }


        WebClient webClient2 = WebClient.create(INSIDERXURL);
        List<InsideRxResponse> insideRxList = new ArrayList<>();
        try {

            if (drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 0).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setProgramId(0);
                drugRequest.setDrugId(drugId + "");
                drugRequest.setBrandIndicator(UIRequestObject.getDrugType());
                drugRequest.setLongitude(insideRxRequest.getLongitude());
                drugRequest.setLatitude(insideRxRequest.getLatitude());
                drugRequest.setNdc(insideRxRequest.getNdc());
                drugRequest.setQuantity(insideRxRequest.getQuantity());
                try {
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                } catch (Exception ex) {

                }
                drugRequestRepository.save(drugRequest);
            } else {

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 0).get(0);
                drugRequest.setProgramId(0);
                drugRequest.setDrugId(drugId + "");
                drugRequest.setBrandIndicator(UIRequestObject.getDrugType());
                drugRequest.setLatitude(insideRxRequest.getLatitude());
                drugRequest.setLongitude(insideRxRequest.getLongitude());
                drugRequest.setNdc(insideRxRequest.getNdc());
                drugRequest.setQuantity(insideRxRequest.getQuantity());
                try {
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                } catch (Exception ex) {

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
                    .body(Mono.just(insideRxRequest), InsideRxRequest.class)
                    .exchange()
                    .flatMapMany(clientResponse -> clientResponse.bodyToFlux(InsideRxResponse.class))
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

    private InsideRxRequest getPostObject(UIRequestObject UIRequestObject, Map<String, String> longitudeLatitude) {
        InsideRxRequest p = new InsideRxRequest();
        p.setLatitude(longitudeLatitude.get("latitude"));
        p.setLongitude(longitudeLatitude.get("longitude"));
        p.setNdc(UIRequestObject.getDrugNDC());
        p.setQuantity(String.valueOf(UIRequestObject.getQuantity()));
        p.setSite_identity("irx");
        return p;
    }

    private Comparator<InsideRxPrice> constructInsideRxComparator() {
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

}
