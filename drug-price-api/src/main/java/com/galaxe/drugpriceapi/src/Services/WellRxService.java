package com.galaxe.drugpriceapi.src.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.WellRxRequest.WellRxRequest;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.WellRxResponse.*;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import com.google.gson.*;
import jdk.nashorn.internal.parser.JSONParser;
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

import static com.galaxe.drugpriceapi.src.Services.KrogerPriceService.isKroger;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

@Component
public class WellRxService {
    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    DrugRequestRepository drugRequestRepository;
    private Gson gson = new Gson();
    private List<Drugs> drugs = new ArrayList<>();

    private Comparator<Drugs> constructWellRxComparator = null;

    private Map<String, WellRxPostObject> wellRxPostObjectMap = new HashMap<>();
    private Map<String, List<Strengths>> wellRxDrugGSNMap = new HashMap<>();
    private final String COOKIE_WELLRX = "ASP.NET_SessionId=b3krzxvpcoqy3yvkmmxysdad; __RequestVerificationToken=oRmuCHDrNMEqKZg9UV3r4iIDsfrhl8ufDkRjv-iQdLL0vK1mMcjBvwWRck8WKKLUEGrnxNjcOiG3UkpEjNMx0AzA_p81; wrxBannerID=1; _ga=GA1.2.1291111346.1555693895; _gid=GA1.2.1161917873.1555693895; _gcl_au=1.1.1411719143.1555693895; _fbp=fb.1.1555693895438.2092435015; b1pi=!CMbSNvIHLL2vAYwvLnpW7/Jj8QPM1+xdT0mf6+N2Vks4Ivb0dySAGjF6u88OryJxc2EHkscC+BoJkuk=; _gat=1";
    private static final String COOKIE_WELLRX2 = "_ga=GA1.2.536278151.1556140629; _gcl_au=1.1.16835471.1556140629; _fbp=fb.1.1556140629715.1934721103; ASP.NET_SessionId=0ti2s11351uorufof45ymctu; __RequestVerificationToken=rdkSym5WxayIvoYy37bFSZd1owaTJqlu9u0pJokH-dlVTXGZYwY9eg9RFrfeqdP_xmzgpyoBFXqYsm1lMv9Kk3d02PQ1; _gid=GA1.2.1463898530.1563455908; _hjIncludedInSample=1; wrxBannerID=3; _gat=1; b1pi=!7Me/iyTCp0tP1KwvLnpW7/Jj8QPM145xnWUzspQDwUQeYGpFVcyf4wxN/DwIs7q5bElV8+jz6F+GtP0=";

    static ArrayList<Price> getWellRXPrices(DrugRequest drugRequest) {
        String url = "https://www.wellrx.com/prescriptions/get-specific-drug";
        RestTemplate template = new RestTemplate();

        WebClient webClient = WebClient.create(url);

//        String uriDrugName = drugRequest.getDrugName().replaceAll("/ |\\//g", "-");

        // Set REST request body
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        JsonObject body = new JsonObject();
        body.addProperty("gsn", drugRequest.getGsn());
        body.addProperty("lat", drugRequest.getLatitude());
        body.addProperty("lng", drugRequest.getLongitude());
        body.addProperty("numdrugs", "1");
        body.addProperty("quantity", drugRequest.getQuantity());
        body.addProperty("bgIndicator", drugRequest.getBrandIndicator().charAt(0) + "");
        body.addProperty("bReference", drugRequest.getDrugName().toUpperCase());
        body.addProperty("ncpdps", "null");
        body.addProperty("BDN", drugRequest.getDrugName().toUpperCase());

        try {
            // Make POST request
            Mono<String> s = webClient
                    .post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Host", "www.wellrx.com")
                    .header("Referer", "https://www.wellrx.com/prescriptions/humatrope/somerset")
                    .header("Cookie", COOKIE_WELLRX2)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body(Mono.just(body.toString()), String.class)
                    .retrieve().bodyToMono(String.class);
            String block = s.block();

            // Build response object
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
                    JsonArray prices = jsonObject.get("Drugs").getAsJsonArray();
                    // Loop through prices in response
                    for (JsonElement price : prices) {
                        JsonObject priceObject = price.getAsJsonObject();
//                        LinkedHashMap priceMap = (LinkedHashMap) price;
                        Price p = new Price();
                        p.setProgramId(3);
                        p.setPharmacy(priceObject.get("PharmacyName").getAsString());
                        p.setPrice(priceObject.get("Price").getAsDouble());
                        p.setUncPrice(null);
                        p.setDrugDetailsId(parseInt(drugRequest.getDrugId()));

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
    public CompletableFuture<List<Drugs>> getWellRxDrugInfo(UIRequestObject UIRequestObject, Map<String, String> longitudeLatitude, String brand_indicator) {
        int drugId = 0;
        try {
            try {
                List<DrugMaster> drugMaster = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode());
                drugId = drugMaster.get(0).getId();

                 if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 2).size() != 0) {

                    DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 2).get(0);
                    if (drugRequest.getDrugName() == null || drugRequest.getDrugName().equals("")) {
                        drugRequest.setDrugName(UIRequestObject.getDrugName());
                        drugRequestRepository.save(drugRequest);
                    }
                    UIRequestObject.setGSN(drugRequest.getGsn());
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

                } else {
                    System.out.println("ELSE STATEMENT");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            List<Strengths> strengths = null;
            String requestedDrug = UIRequestObject.getDrugName().toUpperCase()
                    .replace("/", "-")
                    .replace("WITH PUMP", "")
                    .replace("PUMP", "")
                    .replace("VAGINAL", "")
                    .replace(" PEN", "")
                    .replace("PATCH", "")
                    .replace("HYDROCHLORIDE", "HCL").intern();
            requestedDrug = requestedDrug.trim();
            String drugName = UIRequestObject.getDrugName();

            UIRequestObject.setDrugName(requestedDrug);
            WellRx wellRxFirstAPIResp = new WellRx();

            try {
                String str = getWellRxOutputString4(UIRequestObject, longitudeLatitude).intern();
                UIRequestObject.setDrugName(drugName);
                wellRxFirstAPIResp = gson.fromJson(str, WellRx.class);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Failed RequestName:"+ UIRequestObject.getDrugName());
                System.out.println("Failed RequestDosage:"+ UIRequestObject.getDosageStrength());
                System.out.println("Failed RequestGSN:"+ UIRequestObject.getGSN());

                System.out.println("Failed RequestQuantity:"+ UIRequestObject.getQuantity());
                System.out.println("Failed RequestBrand:"+ UIRequestObject.getDrugType());
            }
            String dose = UIRequestObject.getDosageStrength().toUpperCase();
            if (dose.contains("PUMP")) {
                dose = "PUMP";
            }
            if (wellRxFirstAPIResp.getForms().size() > 0) {
                // wellRxFirstAPIResp

                for (int i = 0; i < wellRxFirstAPIResp.getForms().size(); i++) {

                    String drugForm = wellRxFirstAPIResp.getForms().get(i).getForm().toUpperCase();
                    System.out.println("DRUG FORM BEFORE " + drugForm);
                    try {
                        drugForm = drugForm.replace(drugForm.substring(drugForm.indexOf("("), drugForm.indexOf(")") + 1), "");
                        drugForm = drugForm.trim();

                        System.out.println("DRUG FORM" + drugForm);
                    } catch (Exception ex) {

                    }

                    if (drugForm.contains("PUMP")) {
                        drugForm = "PUMP";
                    }
                    System.out.println(dose);
                    System.out.println("DRUG FORM" + drugForm);

                    if (dose.contains(drugForm)) {
                        System.out.println("HERE");

                        UIRequestObject.setGSN(wellRxFirstAPIResp.getForms().get(i).getGSN());
                        String str2 = getWellRxOutputString2(UIRequestObject, longitudeLatitude, brand_indicator).intern();

                        WellRx wellRxFirstAPIResp2 = gson.fromJson(str2, WellRx.class);

                        CompletableFuture<List<Drugs>> result = getSpecWellRxDrug(wellRxFirstAPIResp2, requestedDrug, new ArrayList<>(), UIRequestObject, longitudeLatitude, brand_indicator);
                        if (result.join().size() != 0) {

                            //INSERT DRUGREQUESET
                            try {
                                drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();
                                if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 2).size() == 0) {
                                    DrugRequest drugRequest = new DrugRequest();
                                    drugRequest.setProgramId(2);
                                    Drugs savedDrug = result.join().get(0);
                                    drugRequest.setGsn(savedDrug.getGSN());
                                    drugRequest.setLatitude(longitudeLatitude.get("latitude"));
                                    drugRequest.setLongitude(longitudeLatitude.get("longitude"));
                                    drugRequest.setQuantity(savedDrug.getQty());
                                    drugRequest.setBrandIndicator(brand_indicator);
                                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                                    drugRequestRepository.save(drugRequest);
                                } else {
                                    DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 2).get(0);
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
                String dosageStrength = UIRequestObject.getDosageStrength().toUpperCase().replace("/24", "").trim().replaceAll("[A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|%|\\s]", "").trim().intern();
                String dosageStrength2 = UIRequestObject.getDosageStrength().toUpperCase().replace("/24", "").trim().replaceAll("[A-Z|a-z|/|(|)|-|MG|MCG|ML|MG-MCG|%]", "").trim().intern();


                strengths.forEach(strength -> {
                    List<String> words = new ArrayList<>();
                    Collections.addAll(words, dosageStrength2.split("[-\\s\\\\]"));
                    words.removeIf(s -> {
                        try {
                            Double.parseDouble(s);
                        } catch (Exception e) {
                            try {
                                parseInt(s);
                            } catch (Exception ex) {
                                return true;
                            }
                            return false;
                        }
                        return false;
                    });

                    String dosage = "";
                    if (UIRequestObject.getDosageStrength().contains("day")) {
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
                                    UIRequestObject.setQuantity(Double.parseDouble(words.get(1)));
                                }

                                return;
                            }
                        } catch (Exception ex) {
                            if (dosage.equalsIgnoreCase(words.get(0))) {
                                obj.setGSN(strength.getGSN());
                                if (Double.parseDouble(words.get(1)) == Double.parseDouble(words.get(0)) / 1000) {

                                } else {
                                    UIRequestObject.setQuantity(Double.parseDouble(words.get(1)));
                                }

                                return;
                            }
                        }
                        try {
                            if (Double.parseDouble(dosage) == Double.parseDouble(words.get(1))) {

                                obj.setGSN(strength.getGSN());
                                if (Double.parseDouble(words.get(0)) == Double.parseDouble(words.get(1)) / 1000) {

                                } else {
                                    UIRequestObject.setQuantity(Double.parseDouble(words.get(0)));
                                }
                                return;
                            }
                        } catch (Exception ex) {
                            if (dosage.equalsIgnoreCase(words.get(1))) {
                                obj.setGSN(strength.getGSN());
                                if (Double.parseDouble(words.get(0)) == Double.parseDouble(words.get(1)) / 1000) {

                                } else {
                                    UIRequestObject.setQuantity(Double.parseDouble(words.get(0)));
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
                obj.setQuantity(String.valueOf(UIRequestObject.getQuantity()));
                obj.setBReference(UIRequestObject.getDrugName().replace("Patch", "").trim());
                obj.setNcpdps("null");
                if (obj.getGSN() != null && !obj.getGSN().isEmpty()) {
                    String wellRxSpecificDrugResponseStr = getWellRxDrugSpecificOutput(obj, UIRequestObject).intern();
                    List<Drugs> wellRxSpecificDrugs = gson.fromJson(wellRxSpecificDrugResponseStr, WellRx.class).getDrugs();
                    /////
                    if (!CollectionUtils.isEmpty(wellRxSpecificDrugs)) {
                        if (constructWellRxComparator == null)
                            constructWellRxComparator = constructWellRxComparator();

                        Collections.sort(wellRxSpecificDrugs, constructWellRxComparator);
                        try {
                            DrugMaster drugMaster = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0);
                            drugMaster.setGsn(obj.getGSN());
                            drugMasterRepository.save(drugMaster);

                            if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 1).size() > 0) {
                                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugMaster.getId()+"", 2).get(0);
                                drugRequest.setGsn(wellRxSpecificDrugs.get(0).getGSN());
                                drugRequestRepository.save(drugRequest);
                                try {
                                    DrugRequest drugRequest2 = drugRequestRepository.findByDrugIdAndProgramId(drugMaster.getId()+"", 3).get(0);
                                    drugRequest2.setGsn(wellRxSpecificDrugs.get(0).getGSN());
                                    drugRequestRepository.save(drugRequest2);
                                } catch (Exception ex) {

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
                    UIRequestObject.setGSN(wellRxFirstAPIResp.getForms().get(i).getGSN());
                    String str2 = getWellRxOutputString2(UIRequestObject, longitudeLatitude, brand_indicator).intern();

                    WellRx wellRxFirstAPIResp2 = gson.fromJson(str2, WellRx.class);

                    CompletableFuture<List<Drugs>> result = getSpecWellRxDrug(wellRxFirstAPIResp2, requestedDrug, new ArrayList<>(), UIRequestObject, longitudeLatitude, brand_indicator);
                    if (result.join().size() != 0) {
                        try {
                            DrugMaster drugMaster = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0);
                            drugMaster.setGsn(result.join().get(0).getGSN());
                            drugMasterRepository.save(drugMaster);
                        } catch (Exception ex) {

                        }
                        return result;
                    }
                }

            }
        }catch (Exception ex){
            return CompletableFuture.completedFuture(drugs);
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

    public CompletableFuture<List<Drugs>> getSpecWellRxDrug(WellRx wellRxFirstAPIResp, String requestedDrug, List<Strengths> strengths, UIRequestObject UIRequestObject, Map<String, String> longitudeLatitude, String brand_indicator) {

        if (!CollectionUtils.isEmpty(wellRxFirstAPIResp.getStrengths())) {
            wellRxDrugGSNMap.put(requestedDrug, wellRxFirstAPIResp.getStrengths());
            strengths = wellRxFirstAPIResp.getStrengths();
        } else {
            return CompletableFuture.completedFuture(drugs);
        }
//        }


        if (strengths != null) {

            WellRxSpecifDrugPost obj = new WellRxSpecifDrugPost();
            String dosageStrength = UIRequestObject.getDosageStrength().toUpperCase().replaceAll("[A-Z|a-z|\\|(|)|/|MG|MCG|ML|MG-MCG|%|\\s]", "").trim().intern();

            String dosageStrength2 = UIRequestObject.getDosageStrength().toUpperCase().replaceAll("[A-Z|a-z|/|(|)|-|MG|MCG|ML|MG-MCG|%]", "").trim().intern();
            strengths.forEach(strength -> {
                List<String> words = new ArrayList<>();
                Collections.addAll(words, dosageStrength2.split("[-\\s\\\\]"));
                words.removeIf(s -> {
                    try {
                        Double.parseDouble(s);
                    } catch (Exception e) {
                        try {
                            parseInt(s);
                        } catch (Exception ex) {
                            return true;
                        }
                        return false;
                    }
                    return false;
                });

                String dosage = "";
                if (UIRequestObject.getDosageStrength().contains("day")) {

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
                                UIRequestObject.setQuantity(Double.parseDouble(words.get(1)));
                            }

                            return;
                        }
                    } catch (Exception ex) {
                        if (dosage.equalsIgnoreCase(words.get(0))) {
                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(1)) == Double.parseDouble(words.get(0)) / 1000) {

                            } else {
                                UIRequestObject.setQuantity(Double.parseDouble(words.get(1)));
                            }

                            return;
                        }
                    }
                    try {
                        if (Double.parseDouble(dosage) == Double.parseDouble(words.get(1))) {
                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(0)) == Double.parseDouble(words.get(1)) / 1000) {

                            } else {
                                UIRequestObject.setQuantity(Double.parseDouble(words.get(0)));
                            }
                            return;
                        }
                    } catch (Exception ex) {
                        if (dosage.equalsIgnoreCase(words.get(1))) {
                            obj.setGSN(strength.getGSN());
                            if (Double.parseDouble(words.get(0)) == Double.parseDouble(words.get(1)) / 1000) {

                            } else {
                                UIRequestObject.setQuantity(Double.parseDouble(words.get(0)));
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
            obj.setQuantity(String.valueOf(UIRequestObject.getQuantity()));
            obj.setBReference(UIRequestObject.getDrugName());
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
    private Comparator<Drugs> constructWellRxComparator() {
        return (obj1, obj2) -> {
            Double price1 = Double.parseDouble(obj1.getPrice());
            Double price2 = Double.parseDouble(obj2.getPrice());

            return price1.compareTo(price2);
        };
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

    private WellRxPostObject constructWellRxPostObject(UIRequestObject UIRequestObject, Map<String, String> longitudeLatitude) {

        if (wellRxPostObjectMap.containsKey(UIRequestObject.getDrugName())) {

            wellRxPostObjectMap.get(UIRequestObject.getDrugName()).setLat(longitudeLatitude.get("latitude"));
            wellRxPostObjectMap.get(UIRequestObject.getDrugName()).setLng(longitudeLatitude.get("longitude"));

            return wellRxPostObjectMap.get(UIRequestObject.getDrugName());
        } else {
            WellRxPostObject obj = new WellRxPostObject();
            obj.setDrugname(UIRequestObject.getDrugName());
            obj.setLat(longitudeLatitude.get("latitude"));
            obj.setLng(longitudeLatitude.get("longitude"));
            obj.setQty("0");
            obj.setNumdrugs("1");
            obj.setNcpdps("null");
            wellRxPostObjectMap.put(UIRequestObject.getDrugName(), obj);
            return obj;
        }
    }
    String getWellRxOutputString2(UIRequestObject UIRequestObject, Map<String, String> longitudeLatitude, String brand_indicator) {
//        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-specific-drug");
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-specific-drug");

        WellRxGSNSearch wellRxGSNSearch = new WellRxGSNSearch();
        wellRxGSNSearch.setGSN(UIRequestObject.getGSN());
        wellRxGSNSearch.setLat(longitudeLatitude.get("latitude"));
        wellRxGSNSearch.setLng(longitudeLatitude.get("longitude"));
        wellRxGSNSearch.setNumdrugs("1");
        wellRxGSNSearch.setQuantity(UIRequestObject.getQuantity() + "");
        wellRxGSNSearch.setBgIndicator(brand_indicator);
        wellRxGSNSearch.setbReference(UIRequestObject.getDrugName());
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

    private String getWellRxDrugSpecificOutput(WellRxSpecifDrugPost wellRxSpecifDrugPost, UIRequestObject UIRequestObject) {
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-specific-drug");

        try {
            int drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();
            if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 2).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setGsn(wellRxSpecifDrugPost.getGSN());
                drugRequest.setBrandIndicator(wellRxSpecifDrugPost.getBgIndicator());
                drugRequest.setDrugName(wellRxSpecifDrugPost.getBReference());
                drugRequest.setLatitude(wellRxSpecifDrugPost.getLat());
                drugRequest.setLongitude(wellRxSpecifDrugPost.getLng());
                drugRequest.setQuantity(wellRxSpecifDrugPost.getQuantity());
                drugRequest.setProgramId(2);
                drugRequest.setDrugId(drugId+"");
                try {
                    drugRequest.setLatitude(UIRequestObject.getLatitude());
                    drugRequest.setLongitude(UIRequestObject.getLongitude());
                }catch (Exception e){

                }
                drugRequestRepository.save(drugRequest);
            }else{
                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 2).get(0) ;
                drugRequest.setGsn(wellRxSpecifDrugPost.getGSN());
                drugRequest.setBrandIndicator(wellRxSpecifDrugPost.getBgIndicator());
                drugRequest.setDrugName(wellRxSpecifDrugPost.getBReference());
                drugRequest.setLatitude(wellRxSpecifDrugPost.getLat());
                drugRequest.setLongitude(wellRxSpecifDrugPost.getLng());
                drugRequest.setQuantity(wellRxSpecifDrugPost.getQuantity());
                drugRequest.setProgramId(2);
                drugRequest.setDrugId(drugId+"");

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
    String getWellRxResult(DrugRequest drugRequest) {
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-brand-generic");
        WellRxRequest wellRxRequest = new WellRxRequest();
        if (drugRequest.getBrandIndicator().equals("BRAND_WITH_GENERIC")) {
            wellRxRequest.setBgIndicator("B");
        } else if (drugRequest.getBrandIndicator().equals("GENERIC")) {
            wellRxRequest.setBgIndicator("G");
        } else {
            wellRxRequest.setBgIndicator(drugRequest.getBrandIndicator());
        }

        wellRxRequest.setDrugname(drugRequest.getDrugName());
        wellRxRequest.setLat(drugRequest.getLatitude());
        wellRxRequest.setLng(drugRequest.getLongitude());
        wellRxRequest.setNcpdps("null");
        wellRxRequest.setNumdrugs("1");


        return webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Referer", "https://www.wellrx.com/prescriptions/lipitor/somerset,%20nj%2008873,%20usa")
                .header("Cookie", COOKIE_WELLRX)
                .header("X-Requested-With", "XMLHttpRequest")
                .body(Mono.just(wellRxRequest), WellRxRequest.class)
                .retrieve().bodyToMono(String.class)
                .block();

    }
    String getWellRxOutputString4(UIRequestObject UIRequestObject, Map<String, String> longitudeLatitude) {
        WebClient webClient = WebClient.create("https://www.wellrx.com/prescriptions/get-brand-generic");
        WellRxRequest wellRxRequest = new WellRxRequest();
        if (UIRequestObject.getDrugType().equals("BRAND_WITH_GENERIC")) {
            wellRxRequest.setBgIndicator("B");
        } else if (UIRequestObject.getDrugType().equals("GENERIC")) {
            wellRxRequest.setBgIndicator("G");
        } else {
            wellRxRequest.setBgIndicator(UIRequestObject.getDrugType());
        }

        wellRxRequest.setDrugname(UIRequestObject.getDrugName());
        wellRxRequest.setLat(longitudeLatitude.get("latitude"));
        wellRxRequest.setLng(longitudeLatitude.get("longitude"));
        wellRxRequest.setNcpdps("null");
        wellRxRequest.setNumdrugs("1");

        try {
            int drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();
            if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 2).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setProgramId(2);
                drugRequest.setDrugId(drugId+"");
                drugRequest.setBrandIndicator(wellRxRequest.getBgIndicator());
                drugRequest.setDrugName(wellRxRequest.getDrugname());
                drugRequest.setLatitude(wellRxRequest.getLat());
                drugRequest.setLongitude(wellRxRequest.getLng());
                drugRequestRepository.save(drugRequest);
            }else{

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 2).get(0);
                drugRequest.setProgramId(2);
                drugRequest.setDrugId(drugId+"");
                drugRequest.setBrandIndicator(wellRxRequest.getBgIndicator());
                drugRequest.setDrugName(wellRxRequest.getDrugname());
                drugRequest.setLatitude(wellRxRequest.getLat());
                drugRequest.setLongitude(wellRxRequest.getLng());
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
                .body(Mono.just(wellRxRequest), WellRxRequest.class)
                .retrieve().bodyToMono(String.class)
                .block();

    }

}
