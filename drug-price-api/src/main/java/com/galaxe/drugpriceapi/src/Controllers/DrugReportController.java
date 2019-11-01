package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Helpers.SavedReportHelper;
import com.galaxe.drugpriceapi.src.Helpers.Token;
import com.galaxe.drugpriceapi.src.Repositories.*;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.GenerateManualReportRequest;
import com.galaxe.drugpriceapi.src.Services.PriceService;
import com.galaxe.drugpriceapi.src.Services.ReportService;
import com.galaxe.drugpriceapi.src.Services.WellRxService;
import com.galaxe.drugpriceapi.src.TableModels.*;


import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@CrossOrigin
@RestController
public class DrugReportController {

    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    PriceRepository priceRepository;
    @Autowired
    ReportDrugsRepository reportDrugsRepository;
    @Autowired
    DrugRequestRepository drugRequestRepository;
    @Autowired
    PriceController priceController;
    @Autowired
    ProfileRepository profileRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    SavedManualReportDetailsRepository savedManualReportDetailsRepository;
    @Autowired
    PriceService priceService;
    @Autowired
    DrugAuthController drugAuthController ;
    @Autowired
    DrugAlertController drugAlertController;
    @Autowired
    DrugRuleController drugRuleController;
    @Autowired
    ReportRowRepository reportRowRepository;
    @Autowired
    EntityManager entityManager;
    @Autowired
    ReportService reportService;

    //REPORTS
    //------------------------------------------------------------------

    @GetMapping(value = "/remove/price/duplicates/{program}")
    public void removeDuplicatePrices(@PathVariable Integer program) throws Throwable {
        Report lastReport = reportRepository.findFirstByOrderByTimestampDesc();

        List<Report_Drugs> report_drugs = reportDrugsRepository.findByReportId(lastReport.getId());
        List<Price> prices  = new ArrayList<>();
        List<Integer> priceIds= new ArrayList<>();
        for (Report_Drugs reportDrug: report_drugs ) {
            priceIds.add(reportDrug.getPriceId());

        }
        prices = priceRepository.findAllById(priceIds);

        List<Integer> drug_ids = new ArrayList<>();
        List<Price> deletePrices = new ArrayList<>();
        for (Price price: prices) {
            if(price.getProgramId() == program){
                if(!drug_ids.contains(price.getDrugDetailsId())){
                    drug_ids.add(price.getDrugDetailsId());
                }else{
                    deletePrices.add(price);
                }
            }


        }
        priceRepository.deleteAll(deletePrices);

    }

    @PostMapping("/edit/drug")
    public void editDrug(@RequestBody DrugMaster drugMaster) {
        DrugMaster old  = drugMasterRepository.findById(drugMaster.getId()).get();
        List<DrugMaster> oldDrugs = drugMasterRepository.findAllByNDCQuantity(old.getNdc(),old.getQuantity());
        for (DrugMaster oldDrug: oldDrugs ) {
            oldDrug.setReportFlag(false);
            drugMasterRepository.save(oldDrug);
        }


        UIRequestObject UIRequestObject = new UIRequestObject();
        UIRequestObject.setDrugName(drugMaster.getName());
        UIRequestObject.setDosageStrength(drugMaster.getDosageStrength());
        UIRequestObject.setQuantity(drugMaster.getQuantity());
        UIRequestObject.setDrugNDC(drugMaster.getNdc());
        UIRequestObject.setReportFlag(drugMaster.getReportFlag());
        addDrug(UIRequestObject);

    }

    @PostMapping("/add/drug")
    public void addDrug(@RequestBody UIRequestObject UIRequestObject){

        List<String> zipCodes = new ArrayList<>();
        zipCodes.add("90036");
        zipCodes.add("30606");
        zipCodes.add("60639");
        zipCodes.add("10023");
        zipCodes.add("75034");
        List<String> longitudes = new ArrayList<>();
        longitudes.add("-118.3520389");
        longitudes.add("-83.4323375");
        longitudes.add("-87.7517295");
        longitudes.add("-73.9800645");
        longitudes.add("-96.8565427");
        List<String> latitudes = new ArrayList<>();
        latitudes.add("34.0664817");
        latitudes.add("33.9448436");
        latitudes.add("41.9225138");
        latitudes.add("40.7769059");
        latitudes.add("33.1376528");

        CompletableFuture<DrugMaster>  loc1  = new CompletableFuture<>();
        CompletableFuture<DrugMaster>  loc2  = new CompletableFuture<>();
        CompletableFuture<DrugMaster>  loc3  = new CompletableFuture<>();
        CompletableFuture<DrugMaster>  loc4  = new CompletableFuture<>();
        CompletableFuture<DrugMaster>  loc5  = new CompletableFuture<>();
        for(int i = 0 ; i<zipCodes.size(); i++){
            UIRequestObject.setZipcode(zipCodes.get(i));
            UIRequestObject.setLongitude(longitudes.get(i));
            UIRequestObject.setLatitude(latitudes.get(i));

            try {
                    if(i ==0){
                        loc1=  CompletableFuture.completedFuture(reportService.makeDrugAndRequests(UIRequestObject));//FAILS
                    }else if(i ==1){
                        loc2=  CompletableFuture.completedFuture(reportService.makeDrugAndRequests(UIRequestObject));
                    }else if(i ==2){
                        loc3=  CompletableFuture.completedFuture(reportService.makeDrugAndRequests(UIRequestObject));
                    }else if(i ==3){
                        loc4=  CompletableFuture.completedFuture(reportService.makeDrugAndRequests(UIRequestObject));
                    }else if(i ==4){
                        loc5=  CompletableFuture.completedFuture(reportService.makeDrugAndRequests(UIRequestObject));
                    }



            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }


        }

        CompletableFuture.allOf(loc1,loc2,loc3,loc4,loc5).join();
    }

    @GetMapping(value = "/reports/getAll")
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @GetMapping(value = "/reports/get/between/{start}/{end}")
    public List<Report> getReportsBetween(@PathVariable String start, @PathVariable String end) throws ParseException {
        String dt = start;// Start date
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(dt));

        String dt2 = end;// Start date
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd-yyyy");
        Calendar c2 = Calendar.getInstance();
        c2.setTime(sdf2.parse(dt2));

        Date d1 = c.getTime();
        Date d2 = c2.getTime();
        List<Report> r = reportRepository.findByBetweenDates(d1, d2);
        return r;
    }

    @GetMapping(value = "/reports/get/date/{date}")
    public List<Report> getReportsByDate(@PathVariable String date) throws ParseException {
//        Date d =  DateFormat.getDateInstance().parse(date);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(d);
        String dt = date;// Start date
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(dt));

        dt = sdf.format(c.getTime());  // dt is now the new date
        String dt2 = date;// Start date
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd-yyyy");
        Calendar c2 = Calendar.getInstance();
        c2.setTime(sdf.parse(dt));
        c2.add(Calendar.DATE, 1);  // number of days to add
        dt = sdf.format(c.getTime());  // dt is now the new date
        Date d1 = c.getTime();
        Date d2 = c2.getTime();
        return reportRepository.findByBetweenDates(d1, d2);
    }

    @GetMapping(value = "/reports/get/drugCount/{drugCount}")
    public List<Report> getReportsByDrugCount(@PathVariable String drugCount) {
        Integer drug_count = Integer.parseInt(drugCount);
        return reportRepository.findByDrugCount(drug_count);
    }

    @GetMapping("/asd/{r}")
    public ResponseEntity<Resource> getReportRows(@PathVariable String r){
        Integer reportId = Integer.parseInt(r);


        List<List<List<String>>> sheets = new ArrayList<>();
        List<String> zipCodes = new ArrayList<>();

        zipCodes.add("92648");
        zipCodes.add("30062");
        zipCodes.add("60657");
        zipCodes.add("07083");
        zipCodes.add("75034");
        for (String zip: zipCodes) {

            List<ReportRow>reportRows = new ArrayList<>();
            reportRows = this.reportRowRepository.exportReportByZipCode(reportId, zip);
            List<List<String>> rows = new ArrayList<>();
            List<String> data0 = new ArrayList<>();
            data0.add("Drug Name");
            data0.add("Drug Rank");
            data0.add("Drug NDC");
            data0.add("Drug GSN");
            data0.add("Dosage Strength");
            data0.add("Quantity");
            data0.add("Zip Code");
            data0.add("InsideRx Price");
            data0.add("InsideRx UNC Price");
            data0.add("InsideRx Pharmacy");
            data0.add("GoodRx Price");
            data0.add("GoodRx Pharmacy");
            data0.add("U.S Pharmacy Card Price");
            data0.add("U.S Pharmacy Card Pharmacy");
            data0.add("WellRx Price");
            data0.add("WellRx Pharmacy");
            data0.add("MedImpact Price");
            data0.add("MedImpact Pharmacy");
            data0.add("Singlecare Price");
            data0.add("Singlecare Pharmacy");
            data0.add("Blink Price");
            data0.add("Blink Pharmacy");
            data0.add("Recommended Price");
            data0.add("Difference Price");
            rows.add(data0);
            for (ReportRow reportRow:reportRows) {

                try{
                    List<String> data = new ArrayList<>();
                    data.add(reportRow.getName());
                    data.add((Integer.parseInt(reportRow.getRank())+1)+"");
                    data.add(reportRow.getNdc());
                    try{
                        DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(reportRow.getDrug_id(),2).get(0);
                        String gsn = drugRequest.getGsn();
                        data.add(gsn);
                    }catch (Exception ex){
                        data.add(reportRow.getGsn());
                    }


                    data.add(reportRow.dosage_strength);
                    data.add(reportRow.quantity);
                    data.add(reportRow.getZip_code());
                    try {
                        data.add(new BigDecimal(reportRow.insiderx_price)
                                .setScale(2, RoundingMode.HALF_UP).toString());
                    } catch (Exception ex) {
                        data.add("N/A");
                    }
                    try{
                        data.add(new BigDecimal(reportRow.unc_price)
                                .setScale(2, RoundingMode.HALF_UP).toString());
                    } catch (Exception ex) {
                        data.add("N/A");
                    }
                    if(reportRow.insiderx_pharmacy != null && !reportRow.insiderx_pharmacy.equals("")) {
                        data.add(reportRow.insiderx_pharmacy);
                    } else {
                        switch (reportRow.getRank()) {
                            case "0":
                                data.add("Walgreens");
                                break;
                            case "1":
                                data.add("Wal-Mart");
                                break;
                            case "2":
                                data.add("CVS");
                                break;
                            case "3":
                                data.add("Other");
                                break;
                            case "4":
                                data.add("Kroger");
                                break;
                            default:
                                data.add("N/A");
                                break;
                        }
                    }
                    try {
                        data.add(new BigDecimal(reportRow.goodrx_price)
                                .setScale(2, RoundingMode.HALF_UP).toString());
                    } catch (Exception ex){
                        data.add("N/A");
                    }
                    if(reportRow.goodrx_pharmacy != null && !reportRow.goodrx_pharmacy.equals("")) {
                        data.add(reportRow.goodrx_pharmacy);
                    } else {
                        switch (reportRow.getRank()) {
                            case "0":
                                data.add("Walgreens");
                                break;
                            case "1":
                                data.add("Wal-Mart");
                                break;
                            case "2":
                                data.add("CVS");
                                break;
                            case "3":
                                data.add("Other");
                                break;
                            case "4":
                                data.add("Kroger");
                                break;
                            default:
                                data.add("N/A");
                                break;
                        }
                    }
                    try {
                        data.add(new BigDecimal(reportRow.pharm_price)
                                .setScale(2, RoundingMode.HALF_UP).toString());
                    } catch (Exception ex){
                        data.add("N/A");
                    }
                    if(reportRow.pharm_pharmacy != null && !reportRow.pharm_pharmacy.equals("")){
                        data.add(reportRow.pharm_pharmacy);
                    }else{
                        switch (reportRow.getRank()) {
                            case "0":
                                data.add("Walgreens");
                                break;
                            case "1":
                                data.add("Wal-Mart");
                                break;
                            case "2":
                                data.add("CVS");
                                break;
                            case "3":
                                data.add("Other");
                                break;
                            case "4":
                                data.add("Kroger");
                                break;
                            default:
                                data.add("N/A");
                                break;
                        }
                    }
                    try{
                        data.add(new BigDecimal(reportRow.wellrx_price)
                                .setScale(2, RoundingMode.HALF_UP).toString());
                    }catch (Exception ex){
                        data.add("N/A");
                    }
                    if(reportRow.wellrx_pharmacy != null && !reportRow.wellrx_pharmacy.equals("")){
                        data.add(reportRow.wellrx_pharmacy);
                    }else{
                        switch (reportRow.getRank()) {
                            case "0":
                                data.add("Walgreens");
                                break;
                            case "1":
                                data.add("Wal-Mart");
                                break;
                            case "2":
                                data.add("CVS");
                                break;
                            case "3":
                                data.add("Other");
                                break;
                            case "4":
                                data.add("Kroger");
                                break;
                            default:
                                data.add("N/A");
                                break;
                        }
                    }
                    try{
                        data.add(new BigDecimal(reportRow.medimpact_price)
                                .setScale(2, RoundingMode.HALF_UP).toString());
                    }catch (Exception ex){
                        data.add("N/A");
                    }
                    if(reportRow.medimpact_pharmacy != null && !reportRow.medimpact_pharmacy.equals("")){
                        data.add(reportRow.medimpact_pharmacy);
                    }else{
                        switch (reportRow.getRank()) {
                            case "0":
                                data.add("Walgreens");
                                break;
                            case "1":
                                data.add("Wal-Mart");
                                break;
                            case "2":
                                data.add("CVS");
                                break;
                            case "3":
                                data.add("Other");
                                break;
                            case "4":
                                data.add("Kroger");
                                break;
                            default:
                                data.add("N/A");
                                break;
                        }
                    }
                    try{
                        data.add(new BigDecimal(reportRow.singlecare_price)
                                .setScale(2, RoundingMode.HALF_UP).toString());
                    }catch (Exception ex){
                        data.add("N/A");
                    }
                    if(reportRow.singlecare_pharmacy != null && !reportRow.singlecare_pharmacy.equals("")){
                        data.add(reportRow.singlecare_pharmacy);
                    }else{
                        switch (reportRow.getRank()) {
                            case "0":
                                data.add("Walgreens");
                                break;
                            case "1":
                                data.add("Wal-Mart");
                                break;
                            case "2":
                                data.add("CVS");
                                break;
                            case "3":
                                data.add("Other");
                                break;
                            case "4":
                                data.add("Kroger");
                                break;
                            default:
                                data.add("N/A");
                                break;
                        }
                    }
                    try{
                        data.add(new BigDecimal(reportRow.blink_price)
                                .setScale(2, RoundingMode.HALF_UP).toString());
                    }catch (Exception ex){
                        data.add("N/A");
                    }
                    if(reportRow.blink_pharmacy != null && !reportRow.blink_pharmacy.equals("")){
                        data.add(reportRow.blink_pharmacy);
                    }else{
                        switch (reportRow.getRank()) {
                            case "0":
                                data.add("Walgreens");
                                break;
                            case "1":
                                data.add("Wal-Mart");
                                break;
                            case "2":
                                data.add("CVS");
                                break;
                            case "3":
                                data.add("Other");
                                break;
                            case "4":
                                data.add("Kroger");
                                break;
                            default:
                                data.add("N/A");
                                break;
                        }
                    }
                    try{
                        data.add(new BigDecimal(reportRow.recommended_price)
                                .setScale(2, RoundingMode.HALF_UP).toString());
                    }catch (Exception ex){
                        data.add("N/A");
                    }
                    try{
                        data.add(new BigDecimal((Double.parseDouble(reportRow.insiderx_price)-Double.parseDouble(reportRow.recommended_price)))
                                .setScale(2, RoundingMode.HALF_UP).toString());
                    }catch (Exception ex){
                        data.add("N/A");

                    }
                    rows.add(data);
                }catch (Exception ex){
                    System.out.println("LEFT OUT OF ROW");
                }
                this.entityManager.detach(reportRow);
            }
            sheets.add(rows);
        }
        return reportService.exportManualReportMultipleSheets(sheets);

    }

    @PostMapping(value = "/report/drug/add")
    public List<Report_Drugs> addDrugToReport(@RequestBody UIRequestObject UIRequestObject, Report report2) throws Throwable {
        int here = 0;
        List<Report_Drugs> report_drugs = new ArrayList<>();
        Map<Integer, Double> providerPrices = new HashMap<>();
        try {

            List<DrugMaster> drugMasterList = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode());
            DrugMaster drugMaster = drugMasterList.get(drugMasterList.size() - 1);

            here = 1;
            try {
                UIRequestObject.setGSN(drugMaster.getGsn());
            }
            catch (Exception e){
                e.printStackTrace();
            }
            List<Price> prices = new ArrayList<>();

           try{
              prices =  priceService.getDetails(UIRequestObject,drugMaster).getPrices();
              prices.removeIf(price -> price ==null);
           Double lowestPrice = null ;
            for (Price price: prices) {
                if(lowestPrice == null){
                    lowestPrice=  price.getPrice();
                }else{
                    try {
                        if (price != null  && lowestPrice > price.getPrice()) {
                            lowestPrice = price.getPrice();
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                try {

                    Report_Drugs report_drug = new Report_Drugs();
                    try {
                        Double p = priceRepository.findLastPrice(price.getDrugDetailsId(), price.getProgramId()).get(0).getPrice();
                        Double diff = p - price.getPrice();
                        price.setDifference(diff);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    Price newPrice = priceRepository.save(price);

                    report_drug.setPriceId(newPrice.getId());

                    report_drug.setReportId(report2.getId());

                    report_drugs.add(report_drug);

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            for (Price price: prices) {
                try{
                price.setRecommendedPrice(lowestPrice);
                price.setLowestMarketPrice(lowestPrice);
                priceRepository.save(price);
                }catch (Exception ex){

                }
            }
           }catch (Exception e){
               e.printStackTrace();
           }
            Report report = reportRepository.findById(report2.getId()).get();
            report.setDrugCount(report.getDrugCount() + 1);
            reportRepository.save(report);

            return reportDrugsRepository.saveAll(report_drugs);

        } catch (Exception e) {
            e.printStackTrace();
            if(here ==0){
            DrugMaster drugMaster = new DrugMaster();

            drugMaster.setQuantity(UIRequestObject.getQuantity());
            drugMaster.setName(UIRequestObject.getDrugName());

            drugMaster.setNdc(UIRequestObject.getDrugNDC());
            drugMaster.setDrugType(UIRequestObject.getDrugType());
            drugMaster.setZipCode(UIRequestObject.getZipcode());
            String dosageStrength = UIRequestObject.getDosageStrength().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%]", "").trim().intern();
            drugMaster.setDosageStrength(dosageStrength);
            String brandType = priceService.getBrandIndicator(UIRequestObject).intern();

            try {
                drugMaster.setReportFlag(UIRequestObject.getReportFlag());
            }catch (Exception ex ){
                ex.printStackTrace();
            }
            drugMaster.setDrugType(brandType);
            drugMaster = drugMasterRepository.save(drugMaster);

            List<Price> prices = new ArrayList<>();


            for (int i = 0; i < 7; i++) {
                Price p = new Price();
                p.setDrugDetailsId(drugMaster.getId());
                p.setProgramId(i);
                p.setCreatedat(new Date());
                Price updatedPrice = priceService.updatePrice(p);
                if (updatedPrice == null) {

                } else {
                    try {
                        p = priceRepository.save(updatedPrice);
                        providerPrices.put(i, p.getPrice());
                        prices.add(p);
                        Report_Drugs reportDrug = new Report_Drugs();
                        reportDrug.setReportId(report2.getId());
                        reportDrug.setPriceId(p.getId());
                        report_drugs.add(reportDrug);
                    } catch (Exception excep) {
                        excep.printStackTrace();
                    }
                }


            }
            Double lowestPrice = providerPrices.get(0);
            Double averagePrice;
            Double sum = 0.0;
            for (Map.Entry<Integer, Double> entry : providerPrices.entrySet()) {
                Integer key = entry.getKey();
                Double value = entry.getValue();
                sum = sum + entry.getValue();
                try {
                    if (entry.getValue() <= lowestPrice) {
                        lowestPrice = entry.getValue();
                    }
                } catch (Exception ex) {
                    lowestPrice = entry.getValue();
                }
            }
            averagePrice = sum / providerPrices.size();

            for (Price p2 : prices) {
                p2.setRecommendedPrice(lowestPrice);
                p2.setAveragePrice(averagePrice);
                p2.setCreatedat(new Date());
                priceRepository.save(p2);
            }
            }else{
                if(UIRequestObject.getDrugName().equalsIgnoreCase("Genotropin") && UIRequestObject.getDosageStrength().contains("1.6")){System.out.println("Skipped");}

            }


        }
        for (Report_Drugs r : report_drugs
        ) {

            r = reportDrugsRepository.save(r);
        }
        if(UIRequestObject.getDrugName().equalsIgnoreCase("Genotropin") && UIRequestObject.getDosageStrength().contains("1.6")){System.out.println("Saved Report Drugs");}

//        Report report = reportRepository.findById(report2.getId()).get();
        try {
            report2.setDrugCount(report2.getDrugCount() + 1);
        }catch (Exception ex){
            report2.setDrugCount(1);
        }
        reportRepository.save(report2);

        return report_drugs;

    }

    @GetMapping(value = "/report/automatic")
    public Report generateReport() {

        int forward = 0;
        try {

            Report newReport = new Report();
            newReport.setTimestamp(new Date());
            newReport.setDrugCount(0);
            newReport = reportRepository.save(newReport);
            Alert alert = new Alert();
            alert.setDetailedMessage("Batch for Report "+ newReport.getId() +" has started");
            alert.setName("Report "+ newReport.getId() +" Batch Start");

            alert.setTime(new Date());
            drugAlertController.sendAlert(alert);
            forward = 1;

            List<DrugMaster> drugMasterList = drugMasterRepository.findByReportFlagOrderById(true);

            //create a mongoEntity from a distinct drug and prices
            int count = 1;
            for (DrugMaster drugMaster : drugMasterList) {
                //DrugMaster drugMaster = drugMasterRepository.findById(i).get();
                count++;
                UIRequestObject UIRequestObject = new UIRequestObject();
//                drugMasterRepository.saveAll(drugMasterRepository.findAll());
                UIRequestObject.setQuantity(drugMaster.getQuantity());

                UIRequestObject.setDosageStrength(drugMaster.getDosageStrength());
                UIRequestObject.setDrugName(drugMaster.getName());
                UIRequestObject.setDrugNDC(drugMaster.getNdc());
                UIRequestObject.setZipcode(drugMaster.getZipCode());
                UIRequestObject.setReportFlag(drugMaster.getReportFlag());

                String brandType = priceService.getBrandIndicator(UIRequestObject);

                if (brandType.equals("B")) {
                    brandType = "BRAND_WITH_GENERIC";
                } else {
                    brandType = "GENERIC";
                }
                UIRequestObject.setDrugType(brandType);
                try {
                    addDrugToReport(UIRequestObject, newReport);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }


            }
            Alert alert2 = new Alert();
            alert2.setDetailedMessage("Batch for Report "+ newReport.getId() +" has ended successfully");
            alert2.setName("Report "+ newReport.getId() +" Batch Ended");
//            alert2.setName("Batch Ended");
            alert2.setTime(new Date());
            drugAlertController.sendAlert(alert2);


            drugRuleController.checkRules(newReport);

            return newReport;
        } catch (Exception e) {
            e.printStackTrace();
//            if (forward == 0) {
//                masterListTestController.addToMasterList();
//            }
            Alert alert3 = new Alert();
            alert3.setDetailedMessage("Batch for Report has failed");
            alert3.setName("Report Batch Failed");
//            alert3.set("Batch Failed");
            alert3.setTime(new Date());
            drugAlertController.sendAlert(alert3);
        }

        return null;
    }

    @PostMapping(value = "/create/report/manual")//List<List<String>>
    public ResponseEntity<Resource> createManualReport(@RequestBody GenerateManualReportRequest manualrequestObject) {
        //Create rows list
        List<List<String>> rows = new ArrayList<>();
        Profile p1 = new Profile();
        p1.setName(manualrequestObject.getToken());
        Profile user = profileRepository.findByUsername(drugAuthController.authenticateToken(p1).getUsername()).get(0);

        if (manualrequestObject.getIsSaved()) {
            SavedManualReportDetails savedManualReportDetails = new SavedManualReportDetails();
            savedManualReportDetails.setDrug_fields(manualrequestObject.getDrugDetails());
            List<Integer> drugIds = new ArrayList<>();

            for (DrugMaster drugMaster : manualrequestObject.getDrugs()) {
                drugIds.add(drugMaster.getId());
            }
            savedManualReportDetails.setDrug_ids(drugIds);
            savedManualReportDetails.setName(manualrequestObject.getName());
            savedManualReportDetails.setProviders(manualrequestObject.getProviders());
            savedManualReportDetails.setUserId(user.getId());
            savedManualReportDetailsRepository.save(savedManualReportDetails);

        }

        rows.add(reportService.getHeaders(manualrequestObject));
        for (int i = 0; i < manualrequestObject.getDrugs().size(); i++) {//for all of the drugs
            List<String> row = new ArrayList<>();
            DrugMaster drugMaster = drugMasterRepository.findById(manualrequestObject.getDrugs().get(i).getId()).get();


            row.add(drugMaster.getName());

            UIRequestObject UIRequestObject = new UIRequestObject();
            UIRequestObject.setQuantity(drugMaster.getQuantity());
            UIRequestObject.setDrugType(drugMaster.getDrugType());
            UIRequestObject.setDosageStrength(drugMaster.getDosageStrength());
            UIRequestObject.setDrugName(drugMaster.getName());
            UIRequestObject.setDrugNDC(drugMaster.getNdc());
            UIRequestObject.setZipcode(drugMaster.getZipCode());

            Map<String, String> longitudeLatitude = priceService.constructLongLat(UIRequestObject.getZipcode());

            if (manualrequestObject.getDrugDetails().contains("Brand Type")) {
                row.add(drugMaster.getDrugType());
            }
            if (manualrequestObject.getDrugDetails().contains("Dosage Strength")) {
                row.add(drugMaster.getDosageStrength());
            }
            if (manualrequestObject.getDrugDetails().contains("Quantity")) {
                row.add(drugMaster.getQuantity() + "");
            }
            if (manualrequestObject.getDrugDetails().contains("Zip Code")) {
                row.add(drugMaster.getZipCode());
            }
            if (manualrequestObject.getDrugDetails().contains("Recommended Price")) {
                row.add("$"+priceRepository.findByDrugDetailsId(drugMaster.getId()).get(0).getRecommendedPrice() + "");
            }
            if (manualrequestObject.getDrugDetails().contains("Difference")) {
                Double recommended = priceRepository.findByDrugDetailsId(drugMaster.getId()).get(0).getRecommendedPrice();
                Double current = Double.parseDouble(priceService.getPharmacyPrice("InsideRxResponse", drugMaster, UIRequestObject, longitudeLatitude));
                row.add(recommended - current + "");
            }
            for (String s : manualrequestObject.getProviders()) {
                row.add("$"+ priceService.getPharmacyPrice(s, drugMaster, UIRequestObject, longitudeLatitude));
            }

            rows.add(row);
        }

        return reportService.exportManualReport(rows);
        //   return rows;
    }

    @PostMapping("/reports/saved/get")
    public List<SavedReportHelper> getSavedReport(@RequestBody Token token) {
        Profile p1 = new Profile();
        p1.setName(token.getValue());
        Profile p = drugAuthController.authenticateToken(p1);
        p = profileRepository.findByUsername(p.getUsername()).get(0);

        List<SavedReportHelper> savedReportHelper = new ArrayList<>();
        savedReportHelper = reportService.convertToReportDrugs(savedManualReportDetailsRepository.findByUserId(p.getId()));
        return savedReportHelper;
    }

}