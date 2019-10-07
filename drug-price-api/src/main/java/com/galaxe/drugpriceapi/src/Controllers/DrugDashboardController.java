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
    public Dashboard addDrugToDashboard(@RequestBody UIRequestObject UIRequestObject) throws Throwable {
        Dashboard d = new Dashboard();
        DrugMaster drugMaster;
        try {
            drugMaster = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0);
        } catch (Exception e) {
            DrugMaster drugMaster1 = new DrugMaster();
            drugMaster1.setZipCode(UIRequestObject.getZipcode());
            drugMaster1.setDrugType(UIRequestObject.getDrugType());
            drugMaster1.setNdc(UIRequestObject.getDrugNDC());
            drugMaster1.setDosageStrength(UIRequestObject.getDosageStrength());
            drugMaster1.setName(UIRequestObject.getDrugName());
            drugMaster1.setQuantity(UIRequestObject.getQuantity());
            String brandType = priceService.getBrandIndicator(UIRequestObject).intern();
            drugMaster1.setDrugType(brandType);
            drugMaster = drugMasterRepository.save(drugMaster1);

            priceService.createDrugRequests(UIRequestObject);
            //ADD DrugRequstCreation
        }


        d.setDrugMasterId(drugMaster.getId());
        Profile testProfile = new Profile();
        testProfile.setName(UIRequestObject.getToken());
        String username = drugAuthController.authenticateToken(testProfile).getUsername();
        Profile signedInUser = profileRepository.findByUsername(username).get(0);

        d.setUserId(signedInUser.getId());
        priceService.addPrices(UIRequestObject, drugMaster);

        return dashboardRepository.save(d);
    }


}