package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Controllers.DrugReportController;
import com.galaxe.drugpriceapi.src.Helpers.SavedReportHelper;
import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.GenerateManualReportRequest;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.galaxe.drugpriceapi.src.TableModels.SavedManualReportDetails;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    WellRxService wellRxService;
    @Autowired
    DrugRequestRepository drugRequestRepository;

    public ResponseEntity<Resource> exportManualReport(List<List<String>> rows) {
        String fileName = "/home/files/poi-generated-file.xlsx";
//        String fileName = "poi-generated-file.xlsx";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("DrugReport");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

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
        System.out.println("Starting rows");
        for (List<String> row : rows) {
            if (count == 0) {

            } else {
                Row r = sheet.createRow(count);

                int cellCount = 0;
                for (String cell : row) {
                    r.createCell(cellCount).setCellValue(cell);

                    cellCount++;
                }
            }
            count++;
        }
        System.out.println("Rows finished");
        for (int i = 0; i < rows.get(0).size(); i++) {
            sheet.autoSizeColumn(i);

        }
        FileOutputStream fileOut;
        InputStreamResource resource = null;
        try {
            fileOut = new FileOutputStream(fileName);
            resource = new InputStreamResource(new FileInputStream(fileName));
            workbook.write(fileOut);

            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpHeaders headers = new HttpHeaders();
        File file = new File(fileName);
        System.out.println(file.length());
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "poi-generated-file.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(resource);

    }

    public ResponseEntity<Resource> exportManualReportMultipleSheets(List<List<List<String>>> rows) {

        // Production
//        String fileName = "/home/files/poi-generated-file.xlsx";

        // QA/Dev
        String fileName = "poi-generated-file.xlsx";
        Workbook workbook = new XSSFWorkbook();
//        long start = System.currentTimeMillis();
        Sheet sheet1 = workbook.createSheet("92648");
        Sheet sheet2 = workbook.createSheet("30062");
        Sheet sheet3 = workbook.createSheet("60657");
        Sheet sheet4 = workbook.createSheet("07083");
        Sheet sheet5 = workbook.createSheet("75034");

        List<Sheet> sheets = new ArrayList<>();
        sheets.add(sheet1);
        sheets.add(sheet2);
        sheets.add(sheet3);
        sheets.add(sheet4);
        sheets.add(sheet5);

        for (int s = 0 ; s <rows.size();s++) {
            Sheet sheet = sheets.get(s);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            headerFont.setColor(IndexedColors.BLACK.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < rows.get(s).get(0).size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(rows.get(s).get(0).get(i));
                cell.setCellStyle(headerCellStyle);
            }
            int count = 0;
            System.out.println("Starting rows");
            for (List<String> row : rows.get(s)) {
                if (count == 0) {

                } else {
                    Row r = sheet.createRow(count);

                    int cellCount = 0;
                    for (String cell : row) {
                        r.createCell(cellCount).setCellValue(cell);

                        cellCount++;
                    }
                }
                count++;

            }
            System.out.println("Rows finished");

        }

        for (int i = 0; i < sheets.size(); i++) {
            System.out.println("Auto-sizing columns on sheet " + i);
            for (int j = 0; j < 24; j++) {
                sheets.get(i).autoSizeColumn(j);
            }
        }
        System.out.println("Columns resized");

        FileOutputStream fileOut;
        InputStreamResource resource = null;
        try {
//            fileOut = new FileOutputStream(fileName);
//            InputStream fileInputStream = new FileInputStream(fileName);
//            resource = new InputStreamResource(new FileInputStream(fileName));
            fileOut = new FileOutputStream("poi-generated-file.xlsx");
            fileOut.flush();
            InputStream fileInputStream = new FileInputStream("poi-generated-file.xlsx");
            resource = new InputStreamResource(fileInputStream);
            workbook.write(fileOut);

            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpHeaders headers = new HttpHeaders();
        File file = new File(fileName);
        System.out.println(file.length());
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "poi-generated-file.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                // .contentLength(resumelength)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);

    }

    public List<SavedReportHelper> convertToReportDrugs(List<SavedManualReportDetails> byUserId) {
        List<SavedReportHelper> drugList = new ArrayList<>();
        for (SavedManualReportDetails saved : byUserId) {
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

    public List<String> getHeaders(GenerateManualReportRequest manualrequestObject) {
        List<String> row = new ArrayList<>();
        row.add("Drug Name");
        row.addAll(manualrequestObject.getDrugDetails());
        row.addAll(manualrequestObject.getProviders());

        return row;
    }

    public DrugMaster makeDrugAndRequests(UIRequestObject UIRequestObject) {
        DrugMaster drugMaster = new DrugMaster();
        Map<String,String> longLat = new HashMap<>();
        longLat.put("longitude", UIRequestObject.getLongitude());
        longLat.put("latitude", UIRequestObject.getLatitude());
        drugMaster.setDosageStrength(UIRequestObject.getDosageStrength());
        drugMaster.setDrugType(UIRequestObject.getDrugType());
        drugMaster.setZipCode(UIRequestObject.getZipcode());
        drugMaster.setReportFlag(UIRequestObject.getReportFlag());
        drugMaster.setNdc(UIRequestObject.getDrugNDC());
        drugMaster.setName(UIRequestObject.getDrugName());
        drugMaster.setQuantity(UIRequestObject.getQuantity());
        Map<String,String> brandTypes = getBrandTypes(UIRequestObject.getDrugType());
        drugMaster = drugMasterRepository.save(drugMaster);
        System.out.println("SAVED DRUG MASTER");
        System.out.println(drugMaster.getName());
        long time = System.nanoTime();
        wellRxService.getWellRxDrugInfo(UIRequestObject, longLat, drugMaster.getDrugType());
        long end = System.nanoTime();
        System.out.println("TIME ELAPSED: " + (end-time));
        //InsideRxResponse Request
        DrugRequest insideRequest = new DrugRequest();
        insideRequest.setNdc(drugMaster.getNdc());
        insideRequest.setDrugName(drugMaster.getName());
        insideRequest.setLongitude(UIRequestObject.getLongitude());
        insideRequest.setLatitude(UIRequestObject.getLatitude());
        insideRequest.setQuantity(drugMaster.getQuantity()+"");
        insideRequest.setDrugId(drugMaster.getId()+"");
        insideRequest.setBrandIndicator(brandTypes.get("long"));//BRAND / BRAND_WITH_GENERIC
        insideRequest.setProgramId(0);
        drugRequestRepository.save(insideRequest);

        DrugRequest usPharmRequest = new DrugRequest();
        usPharmRequest.setProgramId(1);
        usPharmRequest.setNdc(drugMaster.getNdc());
        usPharmRequest.setDrugName(drugMaster.getName());
        usPharmRequest.setLongitude(UIRequestObject.getLongitude());
        usPharmRequest.setLatitude(UIRequestObject.getLatitude());
        usPharmRequest.setQuantity(drugMaster.getQuantity()+"");
        usPharmRequest.setDrugId(drugMaster.getId()+"");
        usPharmRequest.setBrandIndicator(brandTypes.get("long"));//BRAND / BRAND_WITH_GENERIC
        drugRequestRepository.save(usPharmRequest);

        DrugRequest medImpactRequest = new DrugRequest();
        medImpactRequest.setProgramId(3);
//        medImpactRequest.setNdc(drugMaster.getNdc());
        medImpactRequest.setDrugName(drugMaster.getName().toUpperCase()
                .replace("/", "-")
                .replace("WITH PUMP", "")
                .replace("PUMP", "")
                .replace("VAGINAL", "")
                .replace(" PEN", "")
                .replace("PATCH", "")
                .replace("HYDROCHLORIDE", "HCL"));
        medImpactRequest.setLongitude(UIRequestObject.getLongitude());
        medImpactRequest.setLatitude(UIRequestObject.getLatitude());
        medImpactRequest.setQuantity(drugMaster.getQuantity()+"");
        medImpactRequest.setDrugId(drugMaster.getId()+"");
        medImpactRequest.setBrandIndicator(brandTypes.get("short"));//BRAND / BRAND_WITH_GENERIC
        try {
            medImpactRequest.setGsn(drugMasterRepository.findById(drugMaster.getId()).get().getGsn());
        }catch (Exception ex){

        }
        drugRequestRepository.save(medImpactRequest);

        DrugRequest singleCareRequest = new DrugRequest();
        singleCareRequest.setProgramId(4);
        singleCareRequest.setNdc(drugMaster.getNdc());
        singleCareRequest.setDrugName(drugMaster.getName());
        singleCareRequest.setLongitude(UIRequestObject.getLongitude());
        singleCareRequest.setLatitude(UIRequestObject.getLatitude());
        singleCareRequest.setQuantity(drugMaster.getQuantity()+"");
        singleCareRequest.setDrugId(drugMaster.getId()+"");
        singleCareRequest.setBrandIndicator(brandTypes.get("long"));//BRAND / BRAND_WITH_GENERIC
        drugRequestRepository.save(singleCareRequest);

        DrugRequest blinkHealth = new DrugRequest();
        singleCareRequest.setProgramId(5);
        singleCareRequest.setNdc(drugMaster.getNdc());
        singleCareRequest.setDrugName(drugMaster.getName().replace(" ", "-"));
        singleCareRequest.setLongitude(UIRequestObject.getLongitude());
        singleCareRequest.setLatitude(UIRequestObject.getLatitude());
        singleCareRequest.setQuantity(drugMaster.getQuantity()+"");
        singleCareRequest.setDrugId(drugMaster.getId()+"");
        singleCareRequest.setBrandIndicator(brandTypes.get("long"));//BRAND / BRAND_WITH_GENERIC
        drugRequestRepository.save(blinkHealth);

        DrugRequest goodRxRequest = new DrugRequest();
        goodRxRequest.setNdc(drugMaster.getNdc());
        goodRxRequest.setDrugName(drugMaster.getName());
        goodRxRequest.setLongitude(UIRequestObject.getLongitude());
        goodRxRequest.setLatitude(UIRequestObject.getLatitude());
        goodRxRequest.setQuantity(drugMaster.getQuantity()+"");
        goodRxRequest.setDrugId(drugMaster.getId()+"");
        goodRxRequest.setBrandIndicator(brandTypes.get("long"));//BRAND / BRAND_WITH_GENERIC
        goodRxRequest.setProgramId(6);
        drugRequestRepository.save(goodRxRequest);

        return drugMaster;
    }

    private Map<String, String> getBrandTypes(String drugType) {
        Map<String, String> brandTypes = new HashMap<>();
        try {
            if (drugType.equals("G") || drugType.equals("GENERIC")) {
                brandTypes.put("long", "GENERIC");
                brandTypes.put("short", "G");
                return brandTypes;
            } else if (drugType.equals("B") || drugType.equals("BRAND_WITH_GENERIC")) {
                brandTypes.put("long", "BRAND_WITH_GENERIC");
                brandTypes.put("short", "B");
                return brandTypes;
            } else {
                brandTypes.put("long", "GENERIC");
                brandTypes.put("short", "G");
                return brandTypes;
            }
        }catch (Exception e){
            brandTypes.put("long", "GENERIC");
            brandTypes.put("short", "G");
            return brandTypes;

        }
    }

}
