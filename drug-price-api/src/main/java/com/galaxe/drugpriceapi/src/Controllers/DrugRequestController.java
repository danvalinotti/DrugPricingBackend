package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public DrugRequest createRequest(@RequestBody DrugRequest drugRequest1) {
        DrugMaster drugMaster = drugMasterRepository.findById(Integer.parseInt(drugRequest1.getDrugId())).get();
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
            drugRequest.setDrugId(drugMasters.get(i).getId()+"");
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
        DrugMaster drugMaster = drugMasterRepository.findById(Integer.parseInt(request.getDrugId())).get();
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


}