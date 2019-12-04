package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.Repositories.PriceRepository;
import com.galaxe.drugpriceapi.src.Repositories.ReportRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.PriceDetails;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.UIResponseObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.Programs;
import com.galaxe.drugpriceapi.src.Services.*;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.Helpers.DrugDescription;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import javax.xml.ws.Response;
import java.util.*;
import static com.galaxe.drugpriceapi.src.Services.RealTimeSearchService.getCompetitorPrices;
import static com.galaxe.drugpriceapi.src.Services.ZipCodeConverter.getCoords;
import static java.lang.Integer.parseInt;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PriceController {
    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    DrugRequestRepository drugRequestRepository;
    @Autowired
    PriceRepository priceRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    PriceService priceService;

    private Boolean flag = setScheduledFutureJob();

    private boolean setScheduledFutureJob() {

        return false;
    }

    @PostMapping("/rts")
    public ResponseEntity realTimeSearch(@RequestBody UIRequestObject request) throws Throwable {
        String ndc = request.getDrugNDC();
        String zipCode = request.getZipcode();
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        try {
            DrugMaster drugMaster = new DrugMaster();
            List<DrugRequest> drugRequests = new ArrayList<>();
            String drugMasterID = "";
            try {
                // Get Drug Master ID from DB
                drugMaster = drugMasterRepository.findByNdc(ndc);
                drugMasterID = drugMaster.getId() + "";
                System.out.println(drugMasterID);
                // Get Drug Requests using DM_ID
                drugRequests = drugRequestRepository.findAllByDrugId(drugMasterID);
            } catch (Exception e) {
                drugMaster = new DrugMaster();
                drugMaster.setZipCode(request.getZipcode());
                drugMaster.setDosageStrength(request.getDosageStrength());
                drugMaster.setDosageUOM("");
                drugMaster.setDrugType(request.getDrugType());
                drugMaster.setName(request.getDrugName());
                drugMaster.setNdc(request.getDrugNDC());
                drugMaster.setGsn(request.getGSN());
                drugMaster.setQuantity(request.getQuantity());

                for (int i = 0; i < 6; i++) {
                    DrugRequest drugRequest = new DrugRequest();
                    drugRequest.setProgramId(i);
                    drugRequest.setDrugType(drugMaster.getDrugType());
                    drugRequest.setDrugName(drugMaster.getName());
                    drugRequest.setDosageStrength(drugMaster.getDosageStrength());
                    drugRequest.setQuantity(drugMaster.getQuantity() + "");
                    drugRequest.setBrandIndicator(request.getBrandIndicator());
                    drugRequest.setNdc(drugMaster.getNdc());
                    drugRequest.setGsn(drugMaster.getGsn());
                    drugRequest.setZipcode(drugMaster.getZipCode());

                    List<String> coords = getCoords(drugRequest.getZipcode());
                    drugRequest.setLatitude(coords.get(0));
                    drugRequest.setLongitude(coords.get(1));

                    drugRequests.add(drugRequest);
                }
            }
            List<List<Price>> prices = new ArrayList<>();
            prices.add(new ArrayList<>());
            prices.add(new ArrayList<>());
            prices.add(new ArrayList<>());
            prices.add(new ArrayList<>());
            prices.add(new ArrayList<>());
            prices.add(new ArrayList<>());
            prices.add(new ArrayList<>());

            // Build REST response object
            response.put("id", drugMasterID);
            response.put("name", drugMaster.getName());
            response.put("dosageStrength", drugMaster.getDosageStrength());
            response.put("dosageUOM", null);
            response.put("quantity", drugMaster.getQuantity());
            response.put("drugType", request.getDrugType());
            response.put("zipCode", drugMaster.getZipCode());
            response.put("ndc", drugMaster.getNdc());
            response.put("gsn", drugMaster.getGsn());

            // General price values
            final double[] currentPrice = {9999.99};
            final double[] lmp = {9999.99};
            final double[] avgSum = {0.0};
            final double[] priceCount = {0.0};

            // Search for each competitor price
            drugRequests.parallelStream().forEach(dr -> {
                // Set location information for Drug Request
                int programId = dr.getProgramId();
                System.out.println("Starting Stream (" + programId + ")");

                dr.setZipcode(zipCode);
                ArrayList<String> coords = getCoords(zipCode);
                if (coords.size() > 0) {
                    dr.setLatitude(coords.get(0));
                    dr.setLongitude(coords.get(1));
                }

                List<Price> price = new ArrayList<>();
                // Call getCompetitorPrices to get price list for program ID and drug master id
                try {
                     price = getCompetitorPrices(dr, programId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Checks if response was received from RTS
                if (price != null) {
                    // Replace null value in prices with programID results
                    prices.set(dr.getProgramId(), price);

                    // Loop through prices for program
                    try {
                        for (int j = 0; j < price.size(); j++) {
                            Price value = price.get(j);
                            // Check if price entry is empty
                            if (value != null && (value.getPrice() != null || value.getUncPrice() != null)) {
                                // Get InsideRX Current Price
                                if (programId == 0) {
                                    // Sets current price to lowest of Price and UNCPrice
                                    if (value.getUncPrice() != null && value.getUncPrice() < value.getPrice()) {
                                        currentPrice[0] = value.getUncPrice();
                                    } else if (currentPrice[0] > value.getPrice()) {
                                        currentPrice[0] = value.getPrice();
                                    }
                                }

                                // Check for new Lowest Market Price
                                if (lmp[0] > value.getPrice()) {
                                    lmp[0] = value.getPrice();
                                }

                                // Add values for calculating average price
                                avgSum[0] += value.getPrice();
                                priceCount[0]++;
                            // Will only replace empty prices if not Blink
                            } else if ((value == null || value.getPrice() == null) && programId != 5) {
                                // Replace empty Price with one from latest report
                                try {
                                    List<Price> fillPrice = getLatestReportPrice(parseInt(dr.getDrugId()), dr.getProgramId());
                                    if (fillPrice != null) {
                                        price.set(j, fillPrice.get(j));
                                        System.out.println("Replaced empty price with latest report price.");
                                    } else {
                                        System.out.println("Report price not found.");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    prices.add(new ArrayList<>());
                }
                System.out.println("Added program ID " + programId);

            });


            // Calculate average price
            double averagePrice = avgSum[0] / priceCount[0];
            double difference = currentPrice[0] - lmp[0];

            System.out.println("Lowest Market Price: " + lmp[0]);
            System.out.println("Current Price: " + currentPrice[0]);
            System.out.println("Average Price: " + averagePrice);
            System.out.println("Difference: " + difference);

            List<Programs> programs = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (prices.get(i) == null) {
                    prices.set(i, new ArrayList<>());
                }
                Programs program = new Programs();
                program.setPrices(new ArrayList<>());

                for (int j = 0; j < prices.get(i).size(); j++) {
                    PriceDetails priceDetails = new PriceDetails();
                    List<PriceDetails> priceDetailsList = program.getPrices();

                    try {
                        priceDetails.setPharmacy(prices.get(i).get(j).getPharmacy());
                        priceDetails.setProgram(prices.get(i).get(j).getProgramId() + "");
                        priceDetails.setPrice(prices.get(i).get(j).getPrice() + "");
                        priceDetails.setUncPrice(prices.get(i).get(j).getUncPrice() + "");
                        priceDetails.setDiff(prices.get(i).get(j).getDifference() + "");
                        priceDetails.setDiffPerc("");
                        priceDetails.setUncPriceFlag(prices.get(i).get(j).getUncPrice() != null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    priceDetailsList.add(priceDetails);

                    program.setPrices(priceDetailsList);
                }
                programs.add(program);
            }

            String desc = "";
            try {
                WebClient webClient = WebClient.create("https://insiderx.com/request/medication/"+ request.getDrugName().toLowerCase().replace(" ", "-")+"/details?locale=en-US");
                DrugDescription description = webClient.get().exchange().flatMap(clientResponse -> clientResponse.bodyToMono(DrugDescription.class)).block();
                assert description != null;
                desc = description.getDescription();
            } catch (Exception e) {
                e.printStackTrace();
                desc = "";
            }

            // Add programs & general price values to response object
            response.put("description", desc);
            response.put("averagePrice", averagePrice);
            response.put("currentPrice", currentPrice[0]);
            response.put("recommendedPrice", lmp[0]);
            response.put("recommendedDiff", difference);
            response.put("programs", programs);

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            // If drug not in DB, use old method to get prices
            e.printStackTrace();
//                UIResponseObject prices = getPharmacyList(request);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

    }

    private List<Price> getLatestReportPrice(Integer drugId, Integer programId) {
        try {
            System.out.println("DrugID: " + drugId + "\nProgramID: " + programId);
            return priceRepository.findLatestPriceForDrug(drugId, programId);
        } catch (Exception ex) {
            System.out.println("No entries recieved from DB.");
            return null;
        }
    }

    //Getting the drug prices for a particular drug
    @PostMapping("/getPharmacyPrice")
    public UIResponseObject getPharmacyList(@RequestBody UIRequestObject UIRequestObject) throws Throwable {
        String drugName = UIRequestObject.getDrugName();
        if (flag) {
            flag = false;
        }
        DrugMaster m = new DrugMaster();

        try {
            m = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            UIRequestObject.setGSN(m.getGsn());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        DrugMaster d ;
        UIResponseObject UIResponseObject = new UIResponseObject();

        List<Price> prices;
        try {


            d = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0);
            System.out.println("NEWEST REPORT ID "+reportRepository.findFirstByOrderByTimestampDesc().getId());
            prices = priceRepository.findRecentPricesByDrugId(d.getId(), reportRepository.findFirstByOrderByTimestampDesc().getId());
            UIResponseObject.setRecommendedDiff("0.00");
            Map<Integer, List<Price>> programPrices = new HashMap<>();

            programPrices.put(0,new ArrayList<>());
            programPrices.put(1,new ArrayList<>());
            programPrices.put(2,new ArrayList<>());
            programPrices.put(3,new ArrayList<>());
            programPrices.put(4,new ArrayList<>());
            programPrices.put(5,new ArrayList<>());
            programPrices.put(6,new ArrayList<>());

            for (Price p : prices) {
                programPrices.get(p.getProgramId()).add(p);
            }
            List<Programs> programs1 = new ArrayList<>();
            for (Map.Entry<Integer, List<Price>> programPrice : programPrices.entrySet()) {
                Programs p = new Programs();
                List<PriceDetails> progs = new ArrayList<>();
                for (Price price: programPrice.getValue()) {
                    PriceDetails prog = new PriceDetails();
                    prog.setPharmacy(price.getPharmacy());
                    prog.setProgram(programIdToString(price.getProgramId()));
                    prog.setDiff(price.getDifference()+"");
                    prog.setPrice(price.getPrice()+"");
                    prog.setUncPrice(price.getUncPrice()+"");
                    if(price.getUncPrice() == null){
                        prog.setUncPriceFlag(false);
                    }else if(price.getUncPrice() >price.getPrice()){
                        prog.setUncPriceFlag(false);
                    }else if(price.getPrice()!=null){
                        prog.setUncPriceFlag(true);
                    }else{
                        prog.setUncPriceFlag(true);
                    }

                    progs.add(prog);

                }
                p.setPrices(progs);
                programs1.add(p);
            }
            UIResponseObject.setPrograms(programs1);
            UIResponseObject.setQuantity(UIRequestObject.getQuantity() + "");
            UIResponseObject.setNdc(UIRequestObject.getDrugNDC());
            UIResponseObject.setDrugType(UIRequestObject.getDrugType());
            UIResponseObject.setDosageStrength(UIRequestObject.getDosageStrength());
            UIResponseObject.setName(UIRequestObject.getDrugName());
            UIResponseObject.setZipcode(UIRequestObject.getZipcode());
            UIResponseObject.setRecommendedPrice(prices.get(0).getRecommendedPrice() + "");
            UIResponseObject.setAverage(prices.get(0).getAveragePrice()+"");

            WebClient webClient = WebClient.create("https://insiderx.com/request/medication/"+ UIRequestObject.getDrugName().toLowerCase().replace(" ", "-")+"/details?locale=en-US");
            DrugDescription description = webClient.get().exchange().flatMap(clientResponse -> clientResponse.bodyToMono(DrugDescription.class)).block();

            UIResponseObject.setDescription(description.getDescription());
            System.out.println("FOUND PRICE FROM DATABASE");
            return UIResponseObject;
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        UIResponseObject finalDrug = priceService.getFinalDrug(UIRequestObject);
        WebClient webClient = WebClient.create("https://insiderx.com/request/medication/"+drugName.toLowerCase().replace(" ", "-").replace("/", "-")+"/details?locale=en-US");
        try {
            DrugDescription description = webClient.get().exchange().flatMap(clientResponse -> clientResponse.bodyToMono(DrugDescription.class)).block();
            finalDrug.setDescription(description.getDescription());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        System.out.println("FOUND PRICE FROM API");
        return finalDrug;
    }

    @GetMapping("/prices/get/all")
    List<Price> getPrices() {
        return priceRepository.findAll();
    }



    private String programIdToString(int programId) {
        switch(programId){
            case 0:
                return "InsideRxResponse";
            case 1:
                return "U.S Pharmacy Card";

            case 2:
                return "WellRx";

            case 3:
                return "MedImpact";

            case 4:
                return "Singlecare";

            case 5:
                return "Blink";

            case 6:
                return "GoodRx";

            default:
                return "";

        }
    }


    //Called when typing in drug name to get suggested Drugs
    @GetMapping("/getDrugInfo/{name}")
    public String getDrugInfo(@PathVariable("name") String name) {

        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }

        WebClient webClient = WebClient.create("https://insiderx.com/request/medications/search?query=" + name + "&limit=8&locale=en-US");

        return priceService.getDrugInfoFromInsideRx(webClient);
    }














}
