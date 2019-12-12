package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.GoodRxResponse.GoodRxResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.galaxe.drugpriceapi.src.Services.KrogerPriceService.isKroger;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

@Component
public class GoodRxService {

    @Autowired
    DrugRequestRepository drugRequestRepository;
    @Autowired
    DrugMasterRepository drugMasterRepository;

    private Gson gson = new Gson();

    public static ArrayList<Price> getGoodRxPrices(DrugRequest drugRequest) {
        // Build API URL and build WebClient
        System.out.println("Start goodrx");
        String dosageStrength = drugRequest.getDosageStrength().replaceAll(" ", "-");
        String url = "https://goodrx.com/api/v4/drugs/" +
                drugRequest.getGood_rx_id() + "/prices" +
                "?location=" + drugRequest.getLongitude() + "," + drugRequest.getLatitude() + "&location_type=LAT_LNG_GEO_IP" +
                "&dosage" + dosageStrength +
                "&quantity=" + ((int) parseDouble(drugRequest.getQuantity()));
        System.out.println(url);
        WebClient webClient = WebClient.create(url);

        try {
            // Send GET request to GoodRX API
            Mono<String> s = webClient
                    .get()
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "www.goodrx.com")
                    .header("Referrer", url)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Cookie", "goodrx-v2=bc89b2e076ae72c2403d29ecd7a111ab8beeba19tU2nGBpz7ZsTCzmag4b5Bb9lBjw9JXF43D36sBAWdN+HJzTXpG2HRAJppxKUrem2bOULWVtqlDO/uPXhZiKkyg/7G9C1Ir9zVdBZbe22rp7s/Exq0CsOZfrvxzRqj8b6gkbHgU29r56Kmj/ZkpmrpiVYWNszOxsflnp8rXo73ZU7vgyBR4K/akPtVrURtfc37dR1Yha9xyL8robbJ83TnOLsMCxoS33DVnTI2xlBtLP9TCs1jpVVd5iGBtBreZG+n+iPWcT9aifeK5OKqpl2HikStAeJCom6WkUyA43ICUOlWMJTDenQY0OkxowB+BEdVrpWIjiH2TOg0bYZ/RmsJ6l4P8AaIFb7jt7gLbNPpjAzZFwKfaGe8Gp4rbb83Pe/VIKzn6TUwFS/93d68KrHaiU7NCrjrfdSK8O3cTlqtwqg4BGY1hqSduRYMBRAFHJkN6EfS7NmXMllPqjPBJNTlnS/TTpXTezAMVQZg4QR8pTBC8AnCzqn1/6f5H6yobJv0JBcgX5YgyughvbgLkQLXXDBGs1c1mICfeunk5tXsSeGN7KVzKTsQa03BSprrCNOSkjH6Mvx37Fvu7DHU1dfr108BVTB84TG4p+5vqb9kMLE4Krr37nWBMpgUCemW8SDJr68h0LLEqCs4izDMGW7RE00/1d8CTjw55A91MJssN9JkT1/uYI5YiaeUf6nJx0rc9cGnTzK1X2HWA6BFLrmcsB5xRP2MNsEWYb3do8Q0x0zVLLwha+CUWwohdGAt5naxXPW/cpIRSK7xw26FGyYmEkpKfvlgLsaY1mbNqp4GC5Esn3A2wGlEtIKBYG5/+f8kgHiVcmZLIvBfT3KULvAh1wFtwlu9qAX5esL+erhJdXxr8RrIJi0b9VPmW3zQxnKCRFOadgONDQwWsp45IkIYNrmWQ==; grx_unique_id=610b6199cdd94d38bb8640586b13bc04; c=; kw=; gclid=; currentLocation={%22city%22:%22Keasbey%22%2C%22state%22:%22NJ%22%2C%22coords%22:{%22latitude%22:40.5143%2C%22longitude%22:-74.30215}%2C%22zip%22:%2208832%22%2C%22source%22:%22session%22%2C%22full_state%22:%22New%20Jersey%22%2C%22formatted_address%22:%22Keasbey%2C%20NJ%22%2C%22distance%22:6}; csrf_token=924a4a79c6f24be385170845cce0e24e; _pxhd=b9e8232b1fafab131e0964322fda8b3c5ea7c3f7ac56bf685175db35f53ffa78:570bc551-0ae1-11ea-a110-9d7f355eed18; myrx_exp_ab_variant=experiment; ppa_exp_ab_variant=experiment")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                    .header("Connection", "keep-alive")
//                    .header("Upgrade-Insecure-Requests", "1")
//                    .header("GRX-API-Client-ID", "8f9b4435-0377-46d7-a898-e1b656649408")
                    .retrieve().bodyToMono(String.class);

            // Extract response and parse JSON
            String block = s.block();
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(Objects.requireNonNull(block));
            ArrayList<Price> pricesByRank = new ArrayList<>(5);

            if (jsonElement.isJsonObject()) {
                // Fill with empty values
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
                    // Extract prices array from API response
                    JsonArray prices = jsonObject.getAsJsonArray("results");
                    // Loop through prices in response
                    for (JsonElement price : prices) {
                        JsonObject priceObject = price.getAsJsonObject();
                        Price p = new Price();
                        p.setProgramId(6);
                        // Get Pharmacy Name and Price from API response JSON object
                        p.setPharmacy(priceObject.getAsJsonObject("pharmacy").get("name").getAsString());
                        p.setPrice(priceObject.get("prices").getAsJsonArray().get(0).getAsJsonObject().get("price").getAsDouble());
                        p.setUncPrice(null);
                        p.setDrugDetailsId(parseInt(drugRequest.getDrugId()));
                        System.out.println("GOODRX PRICES");

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
                        System.out.println("GOODRX SET OTHER PRICE");
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

    public CompletableFuture<GoodRxResponse> getGoodRxPricesOLD(UIRequestObject UIRequestObject) {
        int drugId = 0;
        String str = "";
        DrugRequest drugRequest;
        List<DrugRequest> drugRequests;
        List<DrugMaster> drugMasters;
        if (UIRequestObject.getDrugName().toUpperCase().equals("ATORVASTATIN CALCIUM")) {
            System.out.println("ACYCLOVIR");
        }
        if (UIRequestObject.getDrugName().toUpperCase().equals("CIPROFLOXACIN HCL")) {
            System.out.println("ACYCLOVIR");
        }
        if (UIRequestObject.getDrugName().toUpperCase().equals("CIPROFLOXACIN HYDROCHLORIDE")) {
            System.out.println("ACYCLOVIR");
        }
        if (UIRequestObject.getDrugName().toUpperCase().equals("CITALOPRAM HYDROBROMIDE")) {
            System.out.println("ACYCLOVIR");
        }
        if (UIRequestObject.getDrugName().toUpperCase().equals("CLOMIPHENE CITRATE")) {
            System.out.println("ACYCLOVIR");
        }
        if (UIRequestObject.getDrugName().toUpperCase().equals("ERGOCALCIFEROL")) {
            System.out.println("ACYCLOVIR");
        }

        try {
            drugMasters = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode());
            drugId = drugMasters.get(0).getId();
            if (drugId == 265659) {
                System.out.println("ACYCLOVIR");
            }
            drugRequests = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 6);
            drugRequest = drugRequests.get(0);

            if (drugRequestRepository.findByDrugIdAndProgramId(drugRequest.getDrugId(), 6).size() != 0) {

                WebClient webClient = WebClient.create("https://www.goodrx.com/api/v4/drugs/" + drugRequest.getGood_rx_id() + "/prices?location=" + drugRequest.getLatitude() + "," + drugRequest.getLongitude() + "&location_type=LAT_LNG&distance_mi=6&quantity=" + drugRequest.getQuantity() + "");
                str = webClient
                        .get()
                        .retrieve().bodyToMono(String.class).block();
                GoodRxResponse goodRxResponse = gson.fromJson(str, GoodRxResponse.class);
                try {
                    if (goodRxResponse.getResults().get(0).getPrices().get(0).getPrice() == 0.0) {
                        System.out.println("");
                    }
                } catch (Exception ex) {
                    System.out.println("Caught");
                }
                return CompletableFuture.completedFuture(goodRxResponse);

            } else {
                return CompletableFuture.completedFuture(new GoodRxResponse());
            }
        } catch (Exception ex) {

        }

        return CompletableFuture.completedFuture(new GoodRxResponse());
    }
}
