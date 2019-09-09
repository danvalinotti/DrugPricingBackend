package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
import com.galaxe.drugpriceapi.web.nap.wellRx.WellRxGSNSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@CrossOrigin
@RestController
public class DrugRequestController {

    @Autowired
    DrugRequestRepository drugRequestRepository;

    @Autowired
    DrugMasterRepository drugMasterRepository;

    @GetMapping(value = "/get/requests")
    public List<DrugRequest> getAllReportDrugs() {
        return drugRequestRepository.findAll();
    }
    @GetMapping(value = "/get/requests/group/ndc")
    public List<DrugRequest> getAllGroup() {
        return drugRequestRepository.findAll();
    }
    @PostMapping("/edit/request")
    public void editDrug(@RequestBody DrugMaster drugMaster) {

    }

    @PostMapping(value = "/request/create")
    public DrugRequest createRequest(@RequestBody DrugRequest drugRequest1) {
        DrugMaster drugMaster = drugMasterRepository.findById(drugRequest1.getDrugId()).get();
        List<DrugMaster>drugMasters = drugMasterRepository.findAllByNDCQuantity(drugMaster.getNdc(),drugMaster.getQuantity());

        for (int i = 0 ; i< drugMasters.size();i++) {
            DrugRequest drugRequest = new DrugRequest();
            try {
                drugRequest.setGsn(drugRequest1.getGsn());
            }catch (Exception ex){

            }try {
                drugRequest.setProgramId(drugRequest1.getProgramId());
            }catch (Exception ex){

            }try {
                drugRequest.setBrandIndicator(drugRequest1.getBrandIndicator());
            }catch (Exception ex){

            }try {
                drugRequest.setQuantity(drugRequest1.getQuantity());
            }catch (Exception ex){

            }try {
                drugRequest.setNdc(drugRequest1.getNdc());
            }catch (Exception ex){

            }
            try {
                drugRequest.setDrugName(drugRequest1.getDrugName());
            }catch (Exception ex){

            }try {
                drugRequest.setGood_rx_id(drugRequest1.getGood_rx_id());
            }catch (Exception ex){

            }
            drugRequest.setDrugId(drugMasters.get(i).getId());
            drugRequest.setZipcode(drugMasters.get(i).getZipCode());

            if(drugRequest.getZipcode().equals("90036")){
                drugRequest.setLongitude("-118.3520389");
                drugRequest.setLatitude("34.0664817");
            }
            else if(drugRequest.getZipcode().equals("30606")){
                drugRequest.setLongitude("-83.4323375");
                drugRequest.setLatitude("33.9448436");
            }
            else if(drugRequest.getZipcode().equals("60639")){
                drugRequest.setLongitude("-87.7517295");
                drugRequest.setLatitude("41.9225138");
            }
            else if(drugRequest.getZipcode().equals("10023")){
                drugRequest.setLongitude("-73.9800645");
                drugRequest.setLatitude("40.7769059");
            }
            else if(drugRequest.getZipcode().equals("75034")){
                drugRequest.setLongitude("-96.8565427");
                drugRequest.setLatitude("33.1376528");
            }
            drugRequestRepository.save(drugRequest);
        }
        return drugRequest1;
    }
    @PostMapping(value = "/request/edit")
    public void editRequest(@RequestBody DrugRequest drugRequest) {
        DrugRequest request = drugRequestRepository.findById(drugRequest.getId()).get();
        DrugMaster drugMaster = drugMasterRepository.findById(request.getDrugId()).get();
        List<DrugRequest> drugRequests = drugRequestRepository.findByDrugNDCQuantityAndProgramId(drugMaster.getNdc(),drugMaster.getQuantity(),request.getProgramId());
        for (DrugRequest newDrugRequest:drugRequests) {
            newDrugRequest.setDrugName(drugRequest.getDrugName());
            newDrugRequest.setGsn(drugRequest.getGsn());
//        newDrugRequest.setDrugId(drugRequest.getDrugId());
//        newDrugRequest.setZipcode(drugRequest.getZipcode());
            newDrugRequest.setQuantity(drugRequest.getQuantity());
            newDrugRequest.setNdc(drugRequest.getNdc());
//        newDrugRequest.setLongitude(drugRequest.getLongitude());
//        newDrugRequest.setLatitude(drugRequest.getLatitude());
            newDrugRequest.setBrandIndicator(drugRequest.getBrandIndicator());
            drugRequestRepository.save(newDrugRequest);
        }
    }

    @GetMapping(value = "/update/drugids")
    public void updateDrugIds() {
        List<DrugRequest> drugRequests =  drugRequestRepository.findByProgramId(6);
        for (DrugRequest drugRequest:drugRequests) {
            try{
            DrugMaster drugMaster = drugMasterRepository.findById(drugRequest.getDrugId()).get();
            if(drugMaster.getZipCode().equals(drugRequest.getZipcode())){

            }else{
                DrugMaster newDrugMaster=  drugMasterRepository.findAllByFields(drugMaster.getNdc(),drugMaster.getQuantity(),drugRequest.getZipcode()).get(0);
                drugRequest.setDrugId(newDrugMaster.getId());
            }
            }catch (Exception ex){

            }
        }
        drugRequestRepository.saveAll(drugRequests);
    }
    @GetMapping(value = "/update/locations")
    public void updateLongLatZip() {
       List<DrugRequest> drugRequests =  drugRequestRepository.findAll();
        for (DrugRequest drugRequest:drugRequests) {
            if(drugRequest.getZipcode() == null){
                try {
                    DrugMaster drugMaster = drugMasterRepository.findById(drugRequest.getDrugId()).get();
                    drugRequest.setZipcode(drugMaster.getZipCode());
                }catch (Exception ex){
                    System.out.println("DRUG REQUEST:"+drugRequest.getDrugId());
                }
            }
            try{
            if(drugRequest.getLongitude() == null){
                if(drugRequest.getZipcode().equals("90036")){
                    drugRequest.setLongitude("-118.3520389");
                    drugRequest.setLatitude("34.0664817");
                }
                else if(drugRequest.getZipcode().equals("30606")){
                    drugRequest.setLongitude("-83.4323375");
                    drugRequest.setLatitude("33.9448436");
                }
                else if(drugRequest.getZipcode().equals("60639")){
                    drugRequest.setLongitude("-87.7517295");
                    drugRequest.setLatitude("41.9225138");
                }
                else if(drugRequest.getZipcode().equals("10023")){
                    drugRequest.setLongitude("-73.9800645");
                    drugRequest.setLatitude("40.7769059");
                }
                else if(drugRequest.getZipcode().equals("75034")){
                    drugRequest.setLongitude("-96.8565427");
                    drugRequest.setLatitude("33.1376528");
                }

            }
            }catch (Exception ex){

            }
        }
        drugRequestRepository.saveAll(drugRequests);
    }
    @GetMapping(value = "/remove/failed")
    public void removeGSNNull() {
        List<DrugRequest> drugRequests = drugRequestRepository.findAll();
        for (DrugRequest drugRequest:drugRequests) {
            if((drugRequest.getProgramId() == 2 ||drugRequest.getProgramId() == 3) && drugRequest.getGsn() == null){
                drugRequestRepository.delete(drugRequest);
            }
        }

    }


    @GetMapping(value = "/get/requests/all")
    public List<DrugRequestUI> getDrugRequestObjects() {
       List<DrugRequest> drugRequests=  drugRequestRepository.findAll() ;
       List<DrugRequestUI> requestObjects = new ArrayList<>();

       for (int i = 0;i<drugRequests.size();i++){

           DrugRequestUI drugRequestUI = new DrugRequestUI(drugRequests.get(i));
           DrugRequest drugRequest = drugRequests.get(i);
           int id = drugRequest.getDrugId();
           try {
               DrugMaster drugMaster = drugMasterRepository.findById(id).get();
               drugRequestUI.setDrugMaster(drugMaster);
           }catch (Exception ex){
               System.out.println("DRUG ID NOT FOUND:"+id);
           }
           System.out.println("i:"+i);
           try {
               requestObjects.add(drugRequestUI);
           }catch (Exception ex){

           }
       }

       return requestObjects;
    }
}