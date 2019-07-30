package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxe.drugpriceapi.model.DrugNAP2;
import com.galaxe.drugpriceapi.model.InsideRx;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient2;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient3;
import com.galaxe.drugpriceapi.web.nap.controller.PriceController;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterListService;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterListTestController;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrug;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
import com.galaxe.drugpriceapi.web.nap.singlecare.ExclusivePriceDetails;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import io.jsonwebtoken.Jwts;
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
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@CrossOrigin
@RestController
public class DrugReportController {

    @Autowired
    MasterListService masterListService;
    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    PriceRepository priceRepository;
    @Autowired
    ReportDrugsRepository reportDrugsRepository;
    @Autowired
    PriceController priceController;
    @Autowired
    ProfileRepository profileRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    SavedReportDetailsRepository savedReportDetailsRepository;
    @Autowired
    MasterListTestController masterListTestController;
    @Autowired
    DrugPriceController drugPriceController;
    @Autowired
    DrugAuthController drugAuthController ;
    @Autowired
    DrugAlertController drugAlertController;
    @Autowired
    DrugMasterController drugMasterController;
    @Autowired
    DrugRuleController drugRuleController;
    int count = 0;
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);


    //REPORTS
    //------------------------------------------------------------------
    @GetMapping(value = "/report/create")
    public void createReport() throws Throwable {
        Report lastReport = reportRepository.findFirstByOrderByIdDesc();

        Report newReport = new Report();
        newReport.setTimestamp(new Date());
        newReport.setUserId(1);
        newReport.setDrugCount(lastReport.getDrugCount());
        newReport = reportRepository.save(newReport);
        addDrugsToReport(lastReport, newReport);


    }
    @PostMapping("/report/add/drug/last")
    public void addToLastReport(@RequestBody RequestObject requestObject){

        try {
            addDrugToReport(requestObject,reportRepository.findFirstByOrderByIdDesc());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    @GetMapping(value = "/report/create/empty")
    public void createEmptyReport() throws Throwable {
        Report newReport = new Report();
        newReport.setTimestamp(new Date());
        newReport.setUserId(1);
        newReport.setDrugCount(0);
        newReport.setTimestamp(new Date());
        newReport = reportRepository.save(newReport);
    }

    @GetMapping("/report/create/first")
    public Report createFirstReport() throws Throwable {
        Report report = new Report();
//        Price p = new Price();
//        p = priceRepository.findById(2).get();
        Report_Drugs drugs = new Report_Drugs();
        report.setUserId(1);
        report.setTimestamp(new Date());
        report = reportRepository.save(report);


//        drugs.setReportId(report.getId());
//        drugs.setPriceId(p.getId());
//        drugs = reportDrugsRepository.save(drugs);
        // addDrugsToReport(lastReport, newReport);
        return report;


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

    @GetMapping(value = "/reportsdrugs/getAll")
    public List<Report_Drugs> getAllReportDrugs() {
        return reportDrugsRepository.findAll();
    }

    @GetMapping(value = "/reportdrugs/get/{r}")
    public ResponseEntity<Resource> exportReportById(@PathVariable String r) {
        int reportId = Integer.parseInt(r);

        //Get report drugs by  report id
        List<Report_Drugs> report_drugs = reportDrugsRepository.findByReportId(reportId);

        Set<Integer> distinctDrugs = new HashSet<Integer>();

        for (Report_Drugs reportDrug : report_drugs) {
            distinctDrugs.add(priceRepository.findById(reportDrug.getPriceId()).get().getDrugDetailsId());
        }

        List<MongoEntity> mongoEntities = new ArrayList<>();

        //create a mongoEntity from a distinct drug and prices
        for (Integer i : distinctDrugs) {
            MongoEntity mongoEntity = new MongoEntity();
            DrugMaster drugMaster = drugMasterRepository.findById(i).get();
            mongoEntity.setZipcode(drugMaster.getZipCode());
            mongoEntity.setName(drugMaster.getName());
            mongoEntity.setDosageStrength(drugMaster.getDosageStrength());
            mongoEntity.setDrugType(drugMaster.getDrugType());
            mongoEntity.setNdc(drugMaster.getNdc());
            mongoEntity.setQuantity(drugMaster.getQuantity() + "");

            List<Program> programs = new ArrayList<>();
            Program[] programArr = new Program[6];
            List<Price> prices = priceRepository.findByDrugDetailsId(i);
            mongoEntity.setAverage(prices.get(0).getAveragePrice() + "");
            mongoEntity.setRecommendedPrice(prices.get(0).getRecommendedPrice() + "");
            for (int x = 0; x < programArr.length; x++) {
                Program program = new Program();
                program.setProgram(x + "");
                program.setPrice("N/A");
                program.setPharmacy("N/A");
                //programs.add(program);
                programArr[x] = program;
            }
            for (Price p : prices) {
                Program program = new Program();
                program.setProgram(p.getProgramId() + "");
                program.setPrice(p.getPrice() + "");
                program.setPharmacy(p.getPharmacy());
                //programs.add(program);
                programArr[p.getProgramId()] = program;

                if (Integer.parseInt(program.getProgram()) == 0) {
                    mongoEntity.setRecommendedDiff(Double.parseDouble(mongoEntity.getRecommendedPrice()) - Double.parseDouble(program.getPrice()) + "");
                }
            }
            programs = Arrays.asList(programArr);
            mongoEntity.setPrograms(programs);
            mongoEntities.add(mongoEntity);
        }


        //add to list mongo entity
        return exportReport(mongoEntities);
        //return

    }

    public ResponseEntity<Resource> exportReport(List<MongoEntity> mongoEntities) {

        List<List<String>> rows = new ArrayList<>();
        String[] str = {"Drug Name", "Drug Type", "Dosage Strength",
                "Quantity", "Zip Code", "Inside Rx Price", "U.S Pharmacy Card Price",
                "Well Rx Price", "MedImpact Price", "Singlecare Price",
                "Recommended Price", "Difference"};
        List<String> header = Arrays.asList(str);

        rows.add(header);

        for (MongoEntity element : mongoEntities) {
            List<String> row = new ArrayList<>();

            row.add(element.getName());
            row.add(element.getDrugType());
            row.add(element.getDosageStrength());

            row.add(element.getQuantity());
            row.add(element.getZipcode());
            row.add("$"+element.getPrograms().get(0).getPrice());
            row.add("$"+element.getPrograms().get(1).getPrice());
            row.add("$"+element.getPrograms().get(2).getPrice());
            row.add("$"+element.getPrograms().get(3).getPrice());
            row.add("$"+element.getPrograms().get(4).getPrice());
            row.add("$"+element.getRecommendedPrice());
            row.add("$"+element.getRecommendedDiff());
            rows.add(row);
        }

        return exportManualReport(rows);

    }

    private void addDrugsToReport(Report lastReport, Report newReport) {
        try {
            List<Price> oldReportPrices = drugPriceController.getReportPrices(lastReport);
            List<Price> updatedPrices = drugPriceController.updatePrices(oldReportPrices);
            Map<Integer, Double> providerPrices = new HashMap<>();

            Double lowestPrice = updatedPrices.get(0).getPrice();
            Double averagePrice;
            Double sum = 0.0;
            for (Price p2 : updatedPrices) {


                sum = sum + p2.getPrice();
                if (p2.getPrice() <= lowestPrice) {
                    lowestPrice = p2.getPrice();
                }
            }
            averagePrice = sum / updatedPrices.size();

            for (Price p2 : updatedPrices) {
                p2.setRecommendedPrice(lowestPrice);
                p2.setAveragePrice(averagePrice);
                priceRepository.save(p2);
            }

            for (Price p : updatedPrices) {
                Price savedPrice = priceRepository.save(p);
                Report_Drugs report_drugs = new Report_Drugs();
                report_drugs.setPriceId(savedPrice.getId());
                report_drugs.setReportId(newReport.getId());
                reportDrugsRepository.save(report_drugs);
            }
        } catch (Exception ex) {

        }

    }

    @PostMapping(value = "/report/drug/add")
    public List<Report_Drugs> addDrugToReport(@RequestBody RequestObject requestObject, Report report2) throws Throwable {
       System.out.println("Add Drug");
        int here = 0;
        List<Report_Drugs> report_drugs = new ArrayList<>();
        Map<Integer, Double> providerPrices = new HashMap<>();
        try {

            List<DrugMaster> drugMasterList = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity());
            DrugMaster drugMaster = drugMasterList.get(drugMasterList.size() - 1);
            System.out.println("Add Drug Get drugmaster");
            here = 1;
            try {
                requestObject.setGSN(drugMaster.getGsn());
            }
            catch (Exception e){

            }
            List<Price> prices = new ArrayList<>();

           try{
              prices =  drugMasterController.getDetails(requestObject,drugMaster).getPrices();
           }catch (Exception e){
               System.out.println("NO Prices-------------------------------------------------");
               System.out.println(e.toString());
           }
            System.out.println("Add Drug Get prices");
            for (Price price: prices) {

                Report_Drugs report_drug = new Report_Drugs();
                try {
                    Double p = priceRepository.findLastPrice(price.getDrugDetailsId(), price.getProgramId()).get(0).getPrice();
                    Double diff = p - price.getPrice();
                    price.setDifference(diff);
                }catch (Exception ex){

                }
//                System.out.println("Modified Price");
                Price newPrice = priceRepository.save(price);

//                    System.out.println("added new price");
                    report_drug.setPriceId(newPrice.getId());
                    report_drug.setReportId(report2.getId());
                    report_drugs.add(report_drug);
            }

            Report report = reportRepository.findById(report2.getId()).get();
            report.setDrugCount(report.getDrugCount() + 1);
            reportRepository.save(report);

            return reportDrugsRepository.saveAll(report_drugs);

        } catch (Exception e) {
            if(here ==0){
            DrugMaster drugMaster = new DrugMaster();

            drugMaster.setQuantity(requestObject.getQuantity());
            drugMaster.setName(requestObject.getDrugName());

            drugMaster.setNdc(requestObject.getDrugNDC());
            drugMaster.setDrugType(requestObject.getDrugType());
            drugMaster.setZipCode(requestObject.getZipcode());
            String dosageStrength = requestObject.getDosageStrength().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%]", "").trim().intern();
            drugMaster.setDosageStrength(dosageStrength);
            String brandType = priceController.getBrandIndicator(requestObject).intern();

            try {
                drugMaster.setReportFlag(requestObject.getReportFlag());
            }catch (Exception ex ){

            }
            drugMaster.setDrugType(brandType);
                System.out.println("LINE 399");
            drugMaster = drugMasterRepository.save(drugMaster);

            List<Price> prices = new ArrayList<>();


            for (int i = 0; i < 6; i++) {
                Price p = new Price();
                p.setDrugDetailsId(drugMaster.getId());
                p.setProgramId(i);
                p.setCreatedat(new Date());
                Price updatedPrice = drugPriceController.updatePrice(p);
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
            }


        }
        for (Report_Drugs r : report_drugs
        ) {

            r = reportDrugsRepository.save(r);
        }
        Report report = reportRepository.findById(report2.getId()).get();
        report.setDrugCount(report.getDrugCount() + 1);
        reportRepository.save(report);

        return report_drugs;

    }
    @GetMapping(value = "/test")
    public StringSender test(){
        try {
           masterListService.add();
        }catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @GetMapping(value = "/report/automatic")
    public Report generateReport() {

        int forward = 0;
        try {
            int reportId = reportRepository.findFirstByOrderByIdDesc().getId();
            Report newReport = new Report();
            newReport.setTimestamp(new Date());
            newReport.setDrugCount(0);
            newReport = reportRepository.save(newReport);
            Alert alert = new Alert();
            alert.setDetailedMessage("Batch for Report "+ newReport.getId() +" has started");
            alert.setName("Report "+ newReport.getId() +" Batch Start");
//            alert.setType("Batch Start");
            alert.setTime(new Date());
            drugAlertController.sendAlert(alert);
            forward = 1;

            List<DrugMaster> drugMasterList = drugMasterRepository.findByReportFlagOrderById(true);

            //create a mongoEntity from a distinct drug and prices
            int count = 1;
            for (DrugMaster drugMaster : drugMasterList) {
                System.out.println(drugMaster.getName());
                System.out.println(count + "out of " + drugMasterList.size());
                //DrugMaster drugMaster = drugMasterRepository.findById(i).get();
                count++;
                RequestObject requestObject = new RequestObject();

                requestObject.setQuantity(drugMaster.getQuantity());

                requestObject.setDosageStrength(drugMaster.getDosageStrength());
                requestObject.setDrugName(drugMaster.getName());
                requestObject.setDrugNDC(drugMaster.getNdc());
                requestObject.setZipcode(drugMaster.getZipCode());
                requestObject.setReportFlag(drugMaster.getReportFlag());

                Map<String, String> longitudeLatitude = priceController.constructLongLat(requestObject.getZipcode());
                String brandType = priceController.getBrandIndicator(requestObject);
                if (brandType.equals("B")) {
                    brandType = "BRAND_WITH_GENERIC";
                } else {
                    brandType = "GENERIC";
                }
                requestObject.setDrugType(brandType);
                System.out.println("Before add Drug");
                try {
                    addDrugToReport(requestObject, newReport);
                    System.out.println("After add Drug");
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
            if (forward == 0) {
                masterListTestController.addToMasterList();
            }
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
    public ResponseEntity<Resource> createManualReport(@RequestBody ManualReportRequest2 manualrequestObject) {
        //Create rows list
        List<List<String>> rows = new ArrayList<>();
        Profile p1 = new Profile();
        p1.setName(manualrequestObject.getToken());
        Profile user = profileRepository.findByUsername(drugAuthController.authenticateToken(p1).getUsername()).get(0);

        if (manualrequestObject.getIsSaved()) {
            SavedReportDetails savedReportDetails = new SavedReportDetails();
            savedReportDetails.setDrug_fields(manualrequestObject.getDrugDetails());
            List<Integer> drugIds = new ArrayList<>();

            for (DrugMaster drugMaster : manualrequestObject.getDrugs()) {
                drugIds.add(drugMaster.getId());
            }
            savedReportDetails.setDrug_ids(drugIds);
            savedReportDetails.setName(manualrequestObject.getName());
            savedReportDetails.setProviders(manualrequestObject.getProviders());
            savedReportDetails.setUserId(user.getId());
            savedReportDetailsRepository.save(savedReportDetails);

        }

        rows.add(getHeaders(manualrequestObject));
        for (int i = 0; i < manualrequestObject.getDrugs().size(); i++) {//for all of the drugs
            List<String> row = new ArrayList<>();
            DrugMaster drugMaster = drugMasterRepository.findById(manualrequestObject.getDrugs().get(i).getId()).get();


            row.add(drugMaster.getName());

            RequestObject requestObject = new RequestObject();
            requestObject.setQuantity(drugMaster.getQuantity());
            requestObject.setDrugType(drugMaster.getDrugType());
            requestObject.setDosageStrength(drugMaster.getDosageStrength());
            requestObject.setDrugName(drugMaster.getName());
            requestObject.setDrugNDC(drugMaster.getNdc());
            requestObject.setZipcode(drugMaster.getZipCode());

            Map<String, String> longitudeLatitude = priceController.constructLongLat(requestObject.getZipcode());

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
                Double current = Double.parseDouble(drugPriceController.getPharmacyPrice("InsideRx", drugMaster, requestObject, longitudeLatitude));
                row.add(recommended - current + "");
            }
            for (String s : manualrequestObject.getProviders()) {
                row.add("$"+drugPriceController.getPharmacyPrice(s, drugMaster, requestObject, longitudeLatitude));
            }
//            if (manualrequestObject.getProviders().contains("InsideRx")) {
//                row.add(getPharmacyPrice("InsideRx", drugMaster, requestObject, longitudeLatitude));
//            }
//            if (manualrequestObject.getProviders().contains("WellRx")) {
//                row.add(getPharmacyPrice("WellRx", drugMaster, requestObject, longitudeLatitude));
//            }
//            if (manualrequestObject.getProviders().contains("SingleCare")) {
//                row.add(getPharmacyPrice("SingleCare", drugMaster, requestObject, longitudeLatitude));
//            }
//            if (manualrequestObject.getProviders().contains("MedImpact")) {
//                row.add(getPharmacyPrice("MedImpact", drugMaster, requestObject, longitudeLatitude));
//            }
//            if (manualrequestObject.getProviders().contains("U.S Pharmacy Card")) {
//                row.add(getPharmacyPrice("U.S Pharmacy Card", drugMaster, requestObject, longitudeLatitude));
//            }
            rows.add(row);
        }

        return exportManualReport(rows);
        //   return rows;
    }

    public ResponseEntity<Resource> exportManualReport(List<List<String>> rows) {
        Workbook workbook = new XSSFWorkbook();

        CreationHelper createHelper = workbook.getCreationHelper();

        Sheet sheet = workbook.createSheet("DrugReport");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
//        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < rows.get(0).size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(rows.get(0).get(i));
            cell.setCellStyle(headerCellStyle);
        }
        int count = 0;
        for (List<String> row : rows) {
            if (count == 0) {

            } else {
                Row r = sheet.createRow(count);

                int cellCount = 0;
                for (String cell : row) {
                    r.createCell(cellCount).setCellValue(cell);

                    cellCount++;
                }
                cellCount = 0;
            }
            count++;
        }
        for (int i = 0; i < rows.get(0).size(); i++) {
            sheet.autoSizeColumn(i);

        }
        FileOutputStream fileOut;
        InputStreamResource resource = null;
        try {
            fileOut = new FileOutputStream("poi-generated-file.xlsx");
            InputStream fileInputStream = new FileInputStream("poi-generated-file.xlsx");
            resource = new InputStreamResource(new FileInputStream("poi-generated-file.xlsx"));
            workbook.write(fileOut);

            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpHeaders headers = new HttpHeaders();
        File file = new File("poi-generated-file.xlsx");
        file.length();
        //we are saying we are getting an attachment and what to name it
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "poi-generated-file.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                // .contentLength(resumelength)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(resource);

    }

    @PostMapping("/reports/saved/get")
    public List<SavedReportHelper> getSavedReport(@RequestBody StringSender token) {
        Profile p1 = new Profile();
        p1.setName(token.getValue());
        Profile p = drugAuthController.authenticateToken(p1);
        p = profileRepository.findByUsername(p.getUsername()).get(0);

        List<SavedReportHelper> savedReportHelper = new ArrayList<>();
        savedReportHelper = convertToReportDrugs(savedReportDetailsRepository.findByUserId(p.getId()));
        return savedReportHelper;
    }

    private List<SavedReportHelper> convertToReportDrugs(List<SavedReportDetails> byUserId) {
        List<SavedReportHelper> drugList = new ArrayList<>();
        for (SavedReportDetails saved : byUserId) {
            SavedReportHelper details2 = new SavedReportHelper();
            details2.setDrug_fields(saved.getDrug_fields());

            details2.setName(saved.getName());
            details2.setProviders(saved.getProviders());
            details2.setUserId(saved.getUserId());
            details2.setDrug_ids(drugMasterRepository.findAllById(saved.getDrug_ids()));
            drugList.add(details2);
        }
        return drugList;
    }

    private List<String> getHeaders(ManualReportRequest2 manualrequestObject) {
        List<String> row = new ArrayList<>();
        row.add("Drug Name");
        row.addAll(manualrequestObject.getDrugDetails());
        row.addAll(manualrequestObject.getProviders());

        return row;
    }


}