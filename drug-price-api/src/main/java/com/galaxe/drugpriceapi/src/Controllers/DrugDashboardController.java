package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Helpers.Token;
import com.galaxe.drugpriceapi.src.Repositories.*;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.*;
import com.galaxe.drugpriceapi.src.Services.PriceService;
import com.galaxe.drugpriceapi.src.TableModels.*;

import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.*;


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
    @Autowired
    LeadingDrugsRepository leadingDrugsRepository;
    @Autowired
    TrailingDrugsRepository trailingDrugsRepository;


    @PostMapping(value = "/dashboard/drug/delete")
    public void deleteDashboardDrug(@RequestBody UIResponseObject UIResponseObject) {
        DrugMaster drugMaster = drugMasterRepository.findAllByFields(UIResponseObject.getNdc(), Double.parseDouble(UIResponseObject.getQuantity()), UIResponseObject.getZipcode()).get(0);
        List<Dashboard> dashboards = dashboardRepository.findByDrugMasterId(drugMaster.getId());
        dashboardRepository.deleteAll(dashboards);
    }

    @GetMapping(value = "/dashboard/drugs/trailing")
    public List<LeadingResponseObject> getTrailingDrugs() {
        int reportId = reportRepository.findFirstByOrderByTimestampDesc().getId();
        List<TrailingDrugs> trailingDrugs = trailingDrugsRepository.findByReportId(reportId);
        List<LeadingResponseObject> responseObjects = new ArrayList<>();

        // Loop through all leading drug entries for report Id
        for (TrailingDrugs trailingDrug : trailingDrugs) {
            try {
                LeadingResponseObject response = new LeadingResponseObject();

                DrugMaster drug = drugMasterRepository.findDrugMasterById(trailingDrug.getDrugId());
                response.setName(drug.getName());
                response.setDrugDetailsId(drug.getId());
                response.setDosageStrength(drug.getDosageStrength());
                response.setQuantity(drug.getQuantity());
                response.setNdc(drug.getNdc());
                response.setZipCode(drug.getZipCode());

                Integer[] priceIds = trailingDrug.getPriceIds();
                PriceDetails[] priceDetails = new PriceDetails[7];
                final Double[] lowest = {999.99};
                final int[] nlp = {0};
                List<Price> prices = priceRepository.findAllFromList(Arrays.asList(priceIds));

                // Loop through prices from specific drugId to build programs
                for (int i = 0; i < prices.size(); i++ ) {
                    Price p = prices.get(i);

                    if (p != null) {
                        PriceDetails program = new PriceDetails();
                        program.setProgram(p.getProgramId() + "");
                        program.setDiff(p.getDifference().toString());
                        program.setPharmacy(p.getPharmacy());

//                        if (program.getProgram().equals("1")) {
                            System.out.println(p.getProgramId());
                            System.out.println(p.getPrice());
//                        }


                        if (p.getPrice() == null) {
                            program.setPrice("N/A");
                        } else {
                            program.setPrice(p.getPrice().toString());
                        }

                        if (p.getProgramId() == 0 ) {
                            if (p.getUncPrice() != null) {
                                program.setUncPrice(p.getUncPrice().toString());
                                program.setUncPriceFlag(true);
                            } else {
                                program.setUncPrice(null);
                                program.setUncPriceFlag(false);
                            }
                        } else {
                            if (p.getPrice() != null && p.getPrice() < lowest[0]) {
                                lowest[0] = p.getPrice();
                                nlp[0] = p.getProgramId();
                            }
                            program.setUncPrice("N/A");
                            program.setUncPriceFlag(false);

                        }

                        priceDetails[p.getProgramId()] = program;
                    }
                }

                // Remove null values from programs
                List<PriceDetails> pd = new LinkedList<>(Arrays.asList(priceDetails));
                while(pd.indexOf(null) != -1) {
                    pd.set(pd.indexOf(null), new PriceDetails(
                            "" + pd.indexOf(null),
                            "N/A",
                            "N/A",
                            "N/A",
                            false,
                            "0.00",
                            "0"
                    ));
                }
                response.setPrograms(pd);
                response.setDiff(lowest[0] - response.getPrice());
                if (nlp[0] == 0) {
                    response.setNextLeadingProgram("InsideRx");
                } else if (nlp[0] == 1) {
                    response.setNextLeadingProgram("UsPharmCard");
                } else if (nlp[0] == 2) {
                    response.setNextLeadingProgram("WellRx");
                } else if (nlp[0] == 3) {
                    response.setNextLeadingProgram("MedImpact");
                } else if (nlp[0] == 4) {
                    response.setNextLeadingProgram("SingleCare");
                } else if (nlp[0] == 5) {
                    response.setNextLeadingProgram("Blink");
                } else {
                    response.setNextLeadingProgram("GoodRx");
                }

                responseObjects.add(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return responseObjects;
    }

    @GetMapping(value = "/dashboard/drugs/leading")
    public List<LeadingResponseObject> getLeadDrugs() {
        int reportId = reportRepository.findFirstByOrderByTimestampDesc().getId();
        List<LeadingDrugs> leadingDrugs = leadingDrugsRepository.findByReportId(reportId);
        List<LeadingResponseObject> responseObjects = new ArrayList<>();

        // Loop through all leading drug entries for report Id
        for (LeadingDrugs leadingDrug : leadingDrugs) {
            try {
                LeadingResponseObject response = new LeadingResponseObject();

                DrugMaster drug = drugMasterRepository.findDrugMasterById(leadingDrug.getDrugId());
                response.setName(drug.getName());
                response.setDrugDetailsId(drug.getId());
                response.setDosageStrength(drug.getDosageStrength());
                response.setQuantity(drug.getQuantity());
                response.setNdc(drug.getNdc());
                response.setZipCode(drug.getZipCode());

                Integer[] priceIds = leadingDrug.getPriceIds();
                PriceDetails[] priceDetails = new PriceDetails[7];
                final Double[] lowest = {999.99};
                final int[] nlp = {0};
                List<Price> prices = priceRepository.findAllFromList(Arrays.asList(priceIds));

                // Loop through prices from specific drugId to build programs
                for (int i = 0; i < prices.size(); i++ ) {
                    Price p = prices.get(i);
                    PriceDetails program = new PriceDetails();
                    program.setProgram(p.getProgramId() + "");
                    program.setDiff(p.getDifference().toString());
                    program.setPharmacy(p.getPharmacy());

//                    System.out.println(p.getProgramId());
//                    System.out.println(p.getPrice());

                    if (p.getPrice() == null) {
                        program.setPrice("N/A");
                    } else {
                        program.setPrice(p.getPrice().toString());
                    }

                    if (p.getProgramId() == 0 ) {
                        if (p.getUncPrice() != null) {
                            program.setUncPrice(p.getUncPrice().toString());
                            program.setUncPriceFlag(true);
                        } else {
                            program.setUncPrice(null);
                            program.setUncPriceFlag(false);
                        }
                    } else {
                        if (p.getPrice() != null && p.getPrice() < lowest[0]) {
                            lowest[0] = p.getPrice();
                            nlp[0] = p.getProgramId();
                        }
                        program.setUncPrice("N/A");
                        program.setUncPriceFlag(false);
                    }

                    priceDetails[p.getProgramId()] = program;
                }

                // Remove null values from programs
                List<PriceDetails> pd = new LinkedList<>(Arrays.asList(priceDetails));
                while(pd.indexOf(null) != -1) {
                    pd.set(pd.indexOf(null), new PriceDetails(
                            "" + pd.indexOf(null),
                            "N/A",
                            "N/A",
                            "N/A",
                            false,
                            "0.00",
                            "0"
                    ));
                }
                response.setPrograms(pd);

                response.setDiff(response.getPrice() - lowest[0]);
                if (nlp[0] == 0) {
                    response.setNextLeadingProgram("InsideRx");
                } else if (nlp[0] == 1) {
                    response.setNextLeadingProgram("UsPharmCard");
                } else if (nlp[0] == 2) {
                    response.setNextLeadingProgram("WellRx");
                } else if (nlp[0] == 3) {
                    response.setNextLeadingProgram("MedImpact");
                } else if (nlp[0] == 4) {
                    response.setNextLeadingProgram("SingleCare");
                } else if (nlp[0] == 5) {
                    response.setNextLeadingProgram("Blink");
                } else {
                    response.setNextLeadingProgram("GoodRx");
                }

                responseObjects.add(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return responseObjects;
    }

    @PostMapping("/dashboard/get")
    List<UIResponseObject> getDashboard(@RequestBody Token token) {
        Profile testProfile = new Profile();
        testProfile.setName(token.getValue());
        String userName = drugAuthController.authenticateToken(testProfile).getUsername();
        Profile signedInUser = profileRepository.findByUsername(userName).get(0);
        int userId = signedInUser.getId();
        List<String> drugList = dashboardRepository.findDistinctDrugsByUserId(userId);
        System.out.println(userId);
        drugList.forEach(drug -> System.out.println(drug));
        Integer reportId = reportRepository.findFirstByOrderByTimestampDesc().getId();
        System.out.println("REPORT ID " + reportId);
        List<DrugMaster> drugMasters = new ArrayList<>();
        List<UIResponseObject> mongoEntities = new ArrayList<>();

        for (String s : drugList) {
            try {
                DrugMaster drugMaster = drugMasterRepository.findDrugMasterById(Integer.parseInt(s));
                drugMasters.add(drugMaster);
                UIResponseObject UIResponseObject = new UIResponseObject();

                UIResponseObject.setQuantity(drugMaster.getQuantity() + "");
                //mongoEntity.setPharmacyName("");
                UIResponseObject.setNdc(drugMaster.getNdc());
                UIResponseObject.setDrugType(drugMaster.getDrugType());
                UIResponseObject.setDosageStrength(drugMaster.getDosageStrength());
                UIResponseObject.setName(drugMaster.getName());
                UIResponseObject.setZipcode(drugMaster.getZipCode());

                System.out.println(drugMaster.getId() + ", " + reportId);
                List<Price> prices = priceRepository.findByDrugDetailsIdAndRankAndReportId(drugMaster.getId(),reportId);
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
            drugMaster = drugMasterRepository.findByNDCQuantityZipcode(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode());
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