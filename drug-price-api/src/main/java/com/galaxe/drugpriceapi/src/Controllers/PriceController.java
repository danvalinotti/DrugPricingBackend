package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.PriceRepository;
import com.galaxe.drugpriceapi.src.Repositories.ReportRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.PriceDetails;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.UIResponseObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.Programs;
import com.galaxe.drugpriceapi.src.Services.*;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.Helpers.DrugDescription;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PriceController {
    @Autowired
    DrugMasterRepository drugMasterRepository;
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

    //Getting the drug prices for a particular drug
    @PostMapping("/getPharmacyPrice")
    public UIResponseObject getPharmacyList(@RequestBody UIRequestObject UIRequestObject) throws Throwable {
        String drugName = UIRequestObject.getDrugName();
        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }
        DrugMaster m = new DrugMaster();

        try {
            m = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0);
        } catch (Exception e) {

        }
        try {
            UIRequestObject.setGSN(m.getGsn());
        } catch (Exception ex) {
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
            UIResponseObject.setId(d.getId() + "");
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
                return "InsideRxResponse";

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
