package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Helpers.Token;
import com.galaxe.drugpriceapi.src.Repositories.*;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.UIResponseObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.Programs;
import com.galaxe.drugpriceapi.src.Services.PriceService;
import com.galaxe.drugpriceapi.src.TableModels.Dashboard;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import com.galaxe.drugpriceapi.src.TableModels.Profile;

import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;

import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.PriceDetails;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@CrossOrigin
@RestController
public class DrugDashboardController {
    @Autowired
    DashboardRepository dashboardRepository;
    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    PriceRepository priceRepository;
    @Autowired
    ProfileRepository profileRepository;
    @Autowired
    PriceService priceService;
    @Autowired
    DrugAuthController drugAuthController;
    @Autowired
    ReportRepository reportRepository;


    @PostMapping(value = "/dashboard/drug/delete")
    public void deleteDashboardDrug(@RequestBody UIResponseObject UIResponseObject) {
        DrugMaster drugMaster = drugMasterRepository.findAllByFields(UIResponseObject.getNdc(), Double.parseDouble(UIResponseObject.getQuantity()), UIResponseObject.getZipcode()).get(0);
        List<Dashboard> dashboards = dashboardRepository.findByDrugMasterId(drugMaster.getId());
        dashboardRepository.deleteAll(dashboards);
    }

    @PostMapping("/dashboard/get")
    List<UIResponseObject> getDashboard(@RequestBody Token token) {
        Profile testProfile = new Profile();
        testProfile.setName(token.getValue());
        String userName = drugAuthController.authenticateToken(testProfile).getUsername();
        Profile signedInUser = profileRepository.findByUsername(userName).get(0);
        int userId = signedInUser.getId();
        List<String> drugList = dashboardRepository.findDistinctDrugsByUserId(userId);
        Integer reportId = reportRepository.findFirstByOrderByTimestampDesc().getId();
        List<DrugMaster> drugMasters = new ArrayList<>();
        List<UIResponseObject> mongoEntities = new ArrayList<>();

        for (String s : drugList) {
            try {
                DrugMaster drugMaster = drugMasterRepository.findById(Integer.parseInt(s)).get();
                drugMasters.add(drugMaster);
                UIResponseObject UIResponseObject = new UIResponseObject();

                UIResponseObject.setQuantity(drugMaster.getQuantity() + "");
                //mongoEntity.setPharmacyName("");
                UIResponseObject.setNdc(drugMaster.getNdc());
                UIResponseObject.setDrugType(drugMaster.getDrugType());
                UIResponseObject.setDosageStrength(drugMaster.getDosageStrength());
                UIResponseObject.setName(drugMaster.getName());
                UIResponseObject.setZipcode(drugMaster.getZipCode());

                List<Price> prices = priceRepository.findByDrugDetailsIdAndRankAndReportId(drugMaster.getId(),0,reportId);
                List<Programs> programs = new ArrayList<>();
                UIResponseObject.setRecommendedPrice(prices.get(0).getRecommendedPrice() + "");
                UIResponseObject.setAverage(prices.get(0).getAveragePrice() + "");
                PriceDetails[] programArr = new PriceDetails[7];

                for (int i = 0; i < programArr.length; i++) {
                    PriceDetails program = new PriceDetails();
                    program.setProgram(i + "");
                    program.setPrice("N/A");
                    program.setPharmacy("N/A");
                    programArr[i] = program;
                }
                for (Price p : prices) {
                    PriceDetails program = new PriceDetails();
                    program.setProgram(p.getProgramId() + "");
                    program.setPrice(p.getPrice() + "");
                    program.setPharmacy(p.getPharmacy());
                    programArr[p.getProgramId()] = program;
                }


                List<PriceDetails> programsList = Arrays.asList(programArr);
                List<Programs> programs1 = new ArrayList<>();
                for (PriceDetails prog :programsList) {
                    List<PriceDetails> newList= new ArrayList<>();
                    newList.add(prog);
                    Programs programs2 = new Programs();
                    programs2.setPrices(newList);
                    programs1.add(programs2);
                }
                UIResponseObject.setPrograms(programs1);
                mongoEntities.add(UIResponseObject);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        return mongoEntities;
    }

    @PostMapping(value = "/dashboard/drugs/add")
    @ResponseBody
    public ResponseEntity<HttpStatus> addDrugToDashboard(@RequestBody UIRequestObject UIRequestObject) {
        try {
            // Find drug in drug_master table
            Dashboard d = new Dashboard();
            DrugMaster drugMaster = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0);
            d.setDrugMasterId(drugMaster.getId());

            // Find user in DB and set User ID
            d.setUserId(profileRepository.findByActiveToken(UIRequestObject.getToken()).get(0).getId());

            List<String> existingDrugs = dashboardRepository.findDistinctDrugsByUserId(d.getUserId());

            // Check if drug is already in the dashboard, if so return HTTP 409
            if (existingDrugs.contains(d.getDrugMasterId().toString())) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            } else {
                // Save to dashboard table and send HTTP OK
                dashboardRepository.save(d);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (Exception e) {
            // If drug is not in drug_master
            e.printStackTrace();
            System.out.println(e.getClass().getCanonicalName());
            if (e.getClass().getCanonicalName().equals("java.lang.IndexOutOfBoundsException")) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}