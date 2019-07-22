package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.model.DrugNAP2;
import com.galaxe.drugpriceapi.model.InsideRx;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient2;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient3;
import com.galaxe.drugpriceapi.web.nap.controller.PriceController;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterListTestController;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrug;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


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
    PriceController priceController;
    @Autowired
    ProfileRepository profileRepository;
    @Autowired
    DrugPriceController drugPriceController;
    @Autowired
    DrugAuthController drugAuthController;

    @GetMapping(value = "/dashboard/drugs/get")
    public List<PricesAndMaster> getDashboardDrugs() {
        int profile = 1;
        List<Dashboard> dashboards2 = dashboardRepository.findAll();
        List<Dashboard> dashboards = dashboardRepository.findByUserId(profile);
        List<PricesAndMaster> newList = createPricesAndMasterList(dashboards);

        return newList;

    }

    @PostMapping(value = "/dashboard/drug/delete")
    public void deleteDashboardDrug(@RequestBody MongoEntity mongoEntity) {
        DrugMaster drugMaster = drugMasterRepository.findAllByFields(mongoEntity.getNdc(), Double.parseDouble(mongoEntity.getQuantity())).get(0);
        List<Dashboard> dashboards = dashboardRepository.findByDrugMasterId(drugMaster.getId());
        dashboardRepository.deleteAll(dashboards);
    }

    @PostMapping("/dashboard/get")
    List<MongoEntity> getDashboard(@RequestBody StringSender token) {
        Profile testProfile = new Profile();
        testProfile.setName(token.getValue());
        String userName = drugAuthController.authenticateToken(testProfile).getUsername();
        Profile signedInUser = profileRepository.findByUsername(userName).get(0);
        int userId = signedInUser.getId();
        List<String> drugList = dashboardRepository.findDistinctDrugsByUserId(userId);
        List<DrugMaster> drugMasters = new ArrayList<>();
        List<MongoEntity> mongoEntities = new ArrayList<>();

        for (String s : drugList) {
            DrugMaster drugMaster = drugMasterRepository.findById(Integer.parseInt(s)).get();
            drugMasters.add(drugMaster);
            MongoEntity mongoEntity = new MongoEntity();

            mongoEntity.setQuantity(drugMaster.getQuantity() + "");
            //mongoEntity.setPharmacyName("");
            mongoEntity.setNdc(drugMaster.getNdc());
            mongoEntity.setDrugType(drugMaster.getDrugType());
            mongoEntity.setDosageStrength(drugMaster.getDosageStrength());
            mongoEntity.setName(drugMaster.getName());
            mongoEntity.setZipcode(drugMaster.getZipCode());

            List<Price> prices = priceRepository.findByDrugDetailsId(drugMaster.getId());
            List<Program> programs = new ArrayList<>();
            mongoEntity.setRecommendedPrice(prices.get(0).getRecommendedPrice() + "");
            mongoEntity.setAverage(prices.get(0).getAveragePrice() + "");
            Program[] programArr = new Program[6];

            for (int i = 0; i < programArr.length; i++) {
                Program program = new Program();
                program.setProgram(i + "");
                program.setPrice("N/A");
                program.setPharmacy("N/A");
                //programs.add(program);
                programArr[i] = program;
            }
            for (Price p : prices) {
                Program program = new Program();
                program.setProgram(p.getProgramId() + "");
                program.setPrice(p.getPrice() + "");
                program.setPharmacy(p.getPharmacy());
                //programs.add(program);
                programArr[p.getProgramId()] = program;
            }
            programs = Arrays.asList(programArr);


            mongoEntity.setPrograms(programs);
            mongoEntities.add(mongoEntity);
        }

        return mongoEntities;
    }

    @PostMapping(value = "/dashboard/drugs/add")
    public Dashboard addDrugToDashboard(@RequestBody RequestObject requestObject) throws Throwable {
        Dashboard d = new Dashboard();
        DrugMaster drugMaster;
        try {
            drugMaster = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0);
        } catch (Exception e) {
            DrugMaster drugMaster1 = new DrugMaster();
            drugMaster1.setZipCode(requestObject.getZipcode());
            drugMaster1.setDrugType(requestObject.getDrugType());
            drugMaster1.setNdc(requestObject.getDrugNDC());
            drugMaster1.setDosageStrength(requestObject.getDosageStrength());
            drugMaster1.setName(requestObject.getDrugName());
            drugMaster1.setQuantity(requestObject.getQuantity());
            String brandType = priceController.getBrandIndicator(requestObject).intern();
            drugMaster1.setDrugType(brandType);
            drugMaster = drugMasterRepository.save(drugMaster1);
        }


        d.setDrugMasterId(drugMaster.getId());
        Profile testProfile = new Profile();
        testProfile.setName(requestObject.getToken());
        String username = drugAuthController.authenticateToken(testProfile).getUsername();
        Profile signedInUser = profileRepository.findByUsername(username).get(0);

        d.setUserId(signedInUser.getId());//hardcoded user
        drugPriceController.addPrices(requestObject, drugMaster);
        //reportDrugsRepository.saveAll(addDrugToReport(requestObject));
        return dashboardRepository.save(d);
    }


    private List<PricesAndMaster> createPricesAndMasterList(List<Dashboard> dashboards) {
        List<PricesAndMaster> pricesAndMasters = new ArrayList<>();

        for (Dashboard d : dashboards) {
            PricesAndMaster pricesAndMaster = new PricesAndMaster();
            DrugMaster drugMaster = drugMasterRepository.findById(d.getDrugMasterId()).get();
            List<Price> prices = priceRepository.findByDrugDetailsId(drugMaster.getId());
            pricesAndMaster.setPrices(prices);
            pricesAndMaster.setDrugMaster(drugMaster);
            pricesAndMasters.add(pricesAndMaster);
        }
        return pricesAndMasters;
    }




}