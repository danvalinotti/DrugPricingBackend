package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Helpers.Token;
import com.galaxe.drugpriceapi.src.Repositories.*;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.UIResponseObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.Programs;
import com.galaxe.drugpriceapi.src.Services.PriceService;
import com.galaxe.drugpriceapi.src.TableModels.*;

import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;

import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.PriceDetails;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;


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
    ReportDrugMasterRepository reportDrugMasterRepository;
    @Autowired
    ReportDrugMasterFailedRepository reportDrugMasterFailedRepository;


    @PostMapping(value = "/dashboard/drug/delete")
    public void deleteDashboardDrug(@RequestBody UIResponseObject UIResponseObject) {
        DrugMaster drugMaster = drugMasterRepository.findAllByFields(UIResponseObject.getNdc(), Double.parseDouble(UIResponseObject.getQuantity()), UIResponseObject.getZipcode()).get(0);
        List<Dashboard> dashboards = dashboardRepository.findByDrugMasterId(drugMaster.getId());
        dashboardRepository.deleteAll(dashboards);
    }

    @GetMapping(value = "/dashboard/getAll")
    public ResponseEntity getAllDashboardDrugs() {
        // Get all drugs in report_dm
        List<ReportDrugMaster> reportDrugMasters = reportDrugMasterRepository.findAll();
        List<UIResponseObject> dashboardPrices = new ArrayList<>();

        try {
            // Get latest report ID
            int reportId = reportRepository.findFirstByOrderByTimestampDesc().getId();
            System.out.println(reportId);

            List<Price> drugPrices = priceRepository.findDashboardDrugPrices(reportId);

            reportDrugMasters.forEach(reportDrugMaster -> {
                // Get drug_master entries
//                System.out.println(reportDrugMaster.getDrugId());
                DrugMaster drugMaster = drugMasterRepository.getById(reportDrugMaster.getDrugId());

                if (drugMaster != null) {
                    UIResponseObject responseObject = new UIResponseObject();
                    // Build response object if drug found in drug_master
                    responseObject.setId(drugMaster.getId() + "");
                    responseObject.setName(drugMaster.getName());
                    responseObject.setDosageStrength(drugMaster.getDosageStrength());
                    responseObject.setDosageUOM("null");
                    responseObject.setQuantity(drugMaster.getQuantity() + "");
                    responseObject.setDrugType(drugMaster.getDrugType());
                    responseObject.setZipcode(drugMaster.getZipCode());
                    responseObject.setNdc(drugMaster.getNdc());

                    System.out.println(drugMaster.getZipCode());
//                    System.out.println(drugMaster.getNdc());

                    List<Programs> programs = new ArrayList<>();
                    programs.add(new Programs());
                    programs.add(new Programs());
                    programs.add(new Programs());
                    programs.add(new Programs());
                    programs.add(new Programs());
                    programs.add(new Programs());
                    programs.add(new Programs());
                    programs.get(0).setPrices(new ArrayList<>());
                    programs.get(1).setPrices(new ArrayList<>());
                    programs.get(2).setPrices(new ArrayList<>());
                    programs.get(3).setPrices(new ArrayList<>());
                    programs.get(4).setPrices(new ArrayList<>());
                    programs.get(5).setPrices(new ArrayList<>());
                    programs.get(6).setPrices(new ArrayList<>());

                    try {
                        // Get recent prices for drug entry
//                        List<Price> prices = priceRepository.findByDrugDetailsIdAndRankAndReportId(parseInt(responseObject.getId()), 0, reportId);
//                        System.out.println(drugMaster.getName() + " -> " + prices.size());
                        List<Price> prices = drugPrices.stream()
                                .filter(price -> drugMaster.getId() == price.getDrugDetailsId())
                                .collect(Collectors.toList());

                        if (prices.size() > 0) {
                            responseObject.setAverage(prices.get(0).getAveragePrice() + "");
                            responseObject.setRecommendedPrice(prices.get(0).getRecommendedPrice() + "");


                            for (Price price : prices) {
                                // Set values for new price entry for program
                                PriceDetails priceDetails = new PriceDetails();
                                priceDetails.setProgram(price.getProgramId() + "");
                                priceDetails.setPrice(price.getPrice() + "");
                                priceDetails.setPharmacy(price.getPharmacy());
                                priceDetails.setDiff(price.getDifference() + "");
                                priceDetails.setDiffPerc("");
                                if (price.getUncPrice() != null) {
                                    priceDetails.setUncPrice(price.getUncPrice() + "");
                                    priceDetails.setUncPriceFlag(true);
                                } else {
                                    priceDetails.setUncPrice(null);
                                    priceDetails.setUncPriceFlag(false);
                                }

                                // Get current list of prices for program
                                List<PriceDetails> priceDetailsList = programs.get(parseInt(priceDetails.getProgram())).getPrices();

                                // Add new price to programs list and save to program entry
                                priceDetailsList.add(priceDetails);
                                programs.get(parseInt(priceDetails.getProgram())).setPrices(priceDetailsList);
                            }
                        } else {
                            responseObject.setAverage("");
                            responseObject.setRecommendedPrice("");

                            for (int i = 0; i < 6; i++) {
                                List<PriceDetails> priceDetailsList = new ArrayList<>();
                                PriceDetails p = new PriceDetails();
                                p.setPrice("");
                                p.setPharmacy("");
                                p.setUncPriceFlag(false);
                                p.setUncPrice("");
                                p.setDiffPerc("");
                                p.setDiff("");
                                p.setProgram(i + "");

                                priceDetailsList.add(p);
                                programs.get(i).setPrices(priceDetailsList);
                            }

                        }

                        responseObject.setPrograms(programs);
                        System.out.println("ZIP CODE: " + responseObject.getZipcode());
                        dashboardPrices.add(responseObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

//            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
//            response.put("id", responseObject.getId());
//            response.put("name", responseObject.getName());
//            response.put("dosageStrength", responseObject.getDosageStrength());
//            response.put("dosageUOM", responseObject.getDosageUOM());
//            response.put("quantity", responseObject.getQuantity());
//            response.put("drugType", responseObject.getDrugType());
//            response.put("zipcode", responseObject.getZipcode());
//            response.put("ndc", responseObject.getNdc());
//            response.put("recommendedPrice", responseObject.getRecommendedPrice());
//            response.put("averagePrice", responseObject.getAverage());
//            response.put("programs", responseObject.getPrograms());

            return ResponseEntity.ok().body(dashboardPrices);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
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
                DrugMaster drugMaster = drugMasterRepository.findById(parseInt(s)).get();
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

    @PostMapping(value = "/dashboard/add")
    @ResponseBody
    public ResponseEntity<HttpStatus> addDrugToDashboard(@RequestBody UIRequestObject uiRequestObject) {
        try {
            long numDrugs = reportDrugMasterRepository.count();

            // Check if max drugs (500) exist in report_dm
            if (numDrugs < 2500) {
                // Pulls drugs from 'drug_master' table
                List<DrugMaster> drugMasters = drugMasterRepository.findAllByNDCQuantity(uiRequestObject.getDrugNDC(), uiRequestObject.getQuantity());
                System.out.println("Drug Master Size: " + drugMasters.size());

                // Verifies that requested drug has 5 entries that exist in drug_master
                Boolean verified = verifyReportDrugs(drugMasters);
                if (verified) {
                    for (DrugMaster drugMaster : drugMasters) {
                        ReportDrugMaster reportDrugMaster = new ReportDrugMaster();

                        // Check to see if drug already exists in report_dm
                        List<ReportDrugMaster> existingEntry = reportDrugMasterRepository.findAllByDrugId(drugMaster.getId());

                        if (existingEntry.size() == 0) {
                            // If not, add to report_dm table
                            reportDrugMaster.setDrugId(drugMaster.getId());
                            reportDrugMasterRepository.save(reportDrugMaster);
                            System.out.println("Added drug ID " + drugMaster.getId() + " to report_dm table.");
                        } else {
                            // If found, do nothing
                            System.out.println("Drug already exists in db.");
                        }

                    }

                    // HTTP 202
                    return new ResponseEntity<>(HttpStatus.ACCEPTED);
                } else {
                    // If drug does not exist in drug_master, add to report_dm_failed table
                    System.out.println("VERIFICATION FAILED. Adding drug to report_dm_failed");
                    List<String> zipCodes = new ArrayList<>();
                    zipCodes.add("92648");
                    zipCodes.add("30062");
                    zipCodes.add("60657");
                    zipCodes.add("07083");
                    zipCodes.add("75034");

                    for (DrugMaster drugMaster : drugMasters) {
                        for (String zipCode : zipCodes) {
                            // Create new report_dm_failed entry for each zip code
                            ReportDrugMasterFailed rdmf = new ReportDrugMasterFailed();
                            rdmf.setDosageStrength(uiRequestObject.getDosageStrength());
                            rdmf.setDosageUOM("null");
                            rdmf.setDrugType(uiRequestObject.getDrugType());
                            rdmf.setGsn(uiRequestObject.getGSN());
                            rdmf.setName(uiRequestObject.getDrugName());
                            rdmf.setNdc(uiRequestObject.getDrugNDC());
                            rdmf.setQuantity(uiRequestObject.getQuantity());
                            rdmf.setReportFlag(uiRequestObject.getReportFlag());
                            rdmf.setZipCode(zipCode);

                            reportDrugMasterFailedRepository.save(rdmf);
                        }
                    }

                    // HTTP 200
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INSUFFICIENT_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // HTTP 400
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(value = "/dashboard/remove")
    public ResponseEntity<HttpStatus> removeDrugFromDashboard(@RequestBody UIRequestObject uiRequestObject) {
        try {
            // Make sure drug exists in drug_master
            List<ReportDrugMaster> reportDrugMasters = reportDrugMasterRepository.findAllByDrugId(parseInt(uiRequestObject.getId()));

            if (reportDrugMasters.size() > 0) {
                // Delete drug from DB
                // Return HTTP 200
                reportDrugMasterRepository.delete(reportDrugMasters.get(0));
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                // Drug already deleted or does not exist
                // Return HTTP 208
                return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Return HTTP 500
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Boolean verifyReportDrugs(List<DrugMaster> drugMasters) {
        try {
            boolean pass = true;
            List<String> zipCodes = new ArrayList<>();
            zipCodes.add("92648");
            zipCodes.add("30062");
            zipCodes.add("60657");
            zipCodes.add("07083");
            zipCodes.add("75034");
            for (DrugMaster drugMaster : drugMasters) {
                if (!zipCodes.contains(drugMaster.getZipCode())) {
                    pass = false;
                    break;
                }
            }

            if (pass && drugMasters.size() == 5) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}