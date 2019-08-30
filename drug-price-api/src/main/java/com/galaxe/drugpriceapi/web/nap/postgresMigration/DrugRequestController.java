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
    @PostMapping(value = "/request/create")
    public DrugRequest createRequest(@RequestBody DrugRequest drugRequest) {

        return drugRequestRepository.save(drugRequest);
    }
    @PostMapping(value = "/request/edit")
    public DrugRequest editRequest(@RequestBody DrugRequest drugRequest) {
        DrugRequest newDrugRequest = drugRequestRepository.findById(drugRequest.getId()).get();
        newDrugRequest.setDrugName(drugRequest.getDrugName());
        newDrugRequest.setGsn(drugRequest.getGsn());
//        newDrugRequest.setDrugId(drugRequest.getDrugId());
        newDrugRequest.setZipcode(drugRequest.getZipcode());
        newDrugRequest.setQuantity(drugRequest.getQuantity());
        newDrugRequest.setNdc(drugRequest.getNdc());
        newDrugRequest.setLongitude(drugRequest.getLongitude());
        newDrugRequest.setLatitude(drugRequest.getLatitude());
        newDrugRequest.setBrandIndicator(drugRequest.getBrandIndicator());

        return drugRequestRepository.save(newDrugRequest);
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