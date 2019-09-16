package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
import com.galaxe.drugpriceapi.web.nap.wellRx.WellRxGSNSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
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
    @GetMapping(value = "/update/quantities")
    public void updateQuantities() {
        List<DrugRequest> drugRequests =  drugRequestRepository.findAll();
        for (DrugRequest drugRequest:drugRequests) {
            try{
                if(drugRequest.getQuantity()== null){
                    DrugMaster drugMaster = drugMasterRepository.findById(drugRequest.getDrugId()).get();
                    drugRequest.setQuantity(drugMaster.getQuantity()+"");
                    drugRequestRepository.save(drugRequest);
                }
            }catch (Exception ex){

            }
        }

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
    @GetMapping(value = "/request/add/zero")
    public void addZeros() {
        List<DrugRequest> drugRequests=  drugRequestRepository.findByProgramId(2) ;

        for (int i = 0;i<drugRequests.size();i++){
            DrugRequest drugRequest = drugRequests.get(i);
            if(drugRequest.getGsn().trim().length() == 5){
                drugRequest.setGsn("0"+drugRequest.getGsn().trim());
                drugRequestRepository.save(drugRequest);
            }else if(drugRequest.getGsn().trim().length() == 4){
                drugRequest.setGsn("00"+drugRequest.getGsn().trim());
                drugRequestRepository.save(drugRequest);
            }else if(drugRequest.getGsn().trim().length() == 3){
                drugRequest.setGsn("000"+drugRequest.getGsn().trim());
                drugRequestRepository.save(drugRequest);
            }
        }
    }
//    @GetMapping(value = "/request/blink/id")
//    public void addBlinkIds() {
////        List<DrugRequest> drugRequests=  drugRequestRepository.findByProgramId(5) ;
//        String str = "264078, 263569, 263588, 263607, 263626, 263725, 263645, 263661, 263677, 263693, 263709, 263741, 263757, 263773, 263789, 263805, 263840, 263847, 263854, 263861, 263868, 263875, 263882, 263889, 263896, 263903, 264050, 264057, 264064, 264071, 264260, 263812, 263819, 263826, 263945, 263952, 263959, 263966, 263973, 264036, 264043, 264239, 264246, 263910, 263917, 263924, 263931, 263938, 263987, 263994, 264001, 264008, 264015, 264022, 264029, 264085, 264092, 264099, 264106, 264113, 264120, 264127, 264134, 264141, 264155, 264162, 264169, 264176, 264183, 264190, 264197, 264204, 264211, 264218, 264225, 264232, 264253, 264267, 264274, 264281, 264288, 264295, 264302, 264309, 264316, 264323, 264365, 264372, 264379, 264386, 264393, 264400, 264407, 264414, 264421, 264428, 264470, 264477, 264484, 264491, 264498, 264575, 264582, 264589, 264596, 264603, 264610, 264617, 264624, 264631, 264638, 264645, 264652, 264659, 264666, 264673, 264680, 264687, 264694, 264701, 264708, 264750, 264757, 264764, 264771, 264778, 264785, 264792, 264799, 264806, 264813, 264855, 264435, 264442, 264456, 264463, 264505, 264512, 264519, 264533, 264540, 264554, 264561, 264568, 264330, 264337, 264351, 264358, 264526, 264715, 264722, 264729, 264736, 264743, 264820, 264827, 264834, 264841, 264848, 264862, 264869, 264876, 264883, 264890, 264892, 264894, 264896, 264898, 264900, 264902, 264904, 264906, 264908, 264910, 264917, 264924, 264931, 264938, 264945, 264952, 264959, 264966, 264973, 265085, 265092, 265099, 265106, 265113, 265120, 265127, 265134, 265141, 265148, 265155, 265162, 265169, 265176, 265183, 265190, 265197, 265204, 265211, 265218, 265225, 265232, 265239, 265246, 265253, 265295, 265302, 265309, 265316, 265323, 264987, 264994, 265001, 265050, 265057, 265064, 265071, 265008, 265015, 265022, 265029, 265036, 265043, 265260, 265267, 265274, 265281, 265288, 265330, 265337, 265344, 265351, 265365, 265372, 265379, 265470, 265477, 265484, 265491, 265498, 265505, 265512, 265519, 265526, 265533, 265750, 265757, 265764, 265771, 265778, 265785, 265792, 265799, 265806, 265813, 265820, 265827, 265834, 265841, 265848, 265855, 265862, 265869, 265876, 265883, 265890, 265897, 265904, 265911, 265918, 265925, 265932, 265939, 265946, 265953, 265393, 265400, 265407, 265414, 265421, 265428, 265442, 265449, 265456, 265463, 265540, 265547, 265554, 265561, 265568, 265575, 265582, 265589, 265596, 265603, 265617, 265624, 265631, 265638, 265645, 265652, 265659, 265666, 265673, 265680, 265687, 265694, 265708, 265715, 265722, 265729, 265736, 265743, 265960, 265967, 265974, 265981, 265988, 265995, 266002, 266009, 266016, 266023, 266030, 266037, 266044, 266051, 266058, 266170, 266177, 266184, 266191, 266198, 266205, 266212, 266219, 266226, 266233, 266289, 266296, 266303, 266345, 266352, 266359, 266366, 266373, 266380, 266387, 266394, 266401, 266408, 266268, 266485, 266065, 266072, 266079, 266086, 266093, 266100, 266107, 266121, 266128, 266135, 266142, 266149, 266156, 266163, 266240, 266247, 266310, 266317, 266324, 266331, 266338, 266415, 266422, 266429, 266443, 266450, 266457, 266464, 266471, 266478, 266492, 266499, 266275, 266282, 266590, 266597, 266604, 266611, 266618, 266695, 266702, 266709, 266716, 266723, 266835, 266842, 266849, 266856, 266863, 266905, 266912, 266919, 266926, 266940, 266947, 266954, 266961, 266968, 267010, 267017, 267024, 267031, 267038, 266541, 266562, 266576, 266583, 266625, 266632, 266646, 266653, 266674, 266681, 266688, 266737, 266751, 266786, 266793, 266814, 266821, 266870, 266877, 266884, 266891, 266898, 266933, 266982, 266989, 266996, 267003, 267066, 266520, 266527, 266548, 266555, 266639, 266667, 266730, 266744, 266765, 266772, 266779, 266800, 266807, 266975, 267052, 267073, 267080, 267087, 263550, 273522, 273529, 273540, 273547, 273554, 277139, 263833, 264344, 264449, 264547, 264980, 265078, 266261, 266506, 266534, 266569, 266660, 266828, 267059, 263980, 264148, 265358, 265386, 265435, 265610, 265701, 266114, 266254, 266436, 266513, 266758, 267045, 267094, 267101, 267108";
//        List<String> drug_ids = Arrays.asList(str.split(", "));
////        String blinkIds1= "NA, 207454, 207454, 207454, 207454, 202624, 235013, 235013, 235013, 235013, 235013, 202624, 202624, 202624, 202624, NA, 296359, 296359, 296359, 296359, 296359, 176094, 176094, 176094, 176094, 176094, 222717, 222717, 222717, 222717, 287344, NA, NA, NA, 182198, 182198, 182198, 182198, 182198, 237854, 237854, 183474, 183474, 453501, 453501, 453501, 453501, 453501, 170033, 170033, 170033, 170033, 237854, 237854, 237854, 474324, 474324, 474324, 474324, 474324, 258949, 258949, 258949, 258949, 153566, 153566, 153566, 153566, 153566, 285229, 285229, 285229, 285229, 285229, 183474, 183474, 183474, 287344, 287344, 287344, 287344, 241223, 241223, 241223, 241223, 241223, 229409, 229409, 229409, 229409, 229409, 154892, 154892, 154892, 154892, 154892, 249335, 249335, 249335, 249335, 249335, 237361, 237361, 237361, 237361, 237361, 237361, 237361, 237361, 237361, 237361, 155744, 155744, 155744, 155744, 155744, 168238, 168238, 168238, 168238, 168238, 175833, 175833, 175833, 175833, 175833, 159437, 159437, 159437, 159437";
////        String blinkIds2= "159437, 213323, 269382, 269382, 269382, 269382, 175283, 175283, 175283, 175283, 204885, 204885, 204885, 204885, 244899, 244899, 244899, 244899, 175283, 272021, 272021, 272021, 272021, 272021, 453503, 453503, 453503, 453503, 453503, 213323, 213323, 213323, 213323, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 187550, 187550, 187550, 187550, 187550, 205571, 205571, 205571, 205571, 205571, 163181, 163181, 163181, 163181, 163181, 150773, 150773, 150773, 150773, 150773, 453953, 453953, 453953, 453953, 453953, 217047, 217047, 217047, 217047, 217047, 287768, 287768, 287768, 287768, 287768, 173420, 173420, 173420, 269872, 269872, 269872, 269872, 173420, 199432, 199432, 199432, 199432, 199432, 204513, 204513, 204513, 204513, 204513, 184161, 184161, 184161, 184161, 177730, 177730, 177730, 167106, 167106, 167106, 167106, 167106, 238235, 238235, 238235, 238235, 238235, 218840, 218840, 218840, 218840, 218840, 193624, 193624, 193624";
////        String blinkIds3 = "193624, 193624, 429993, 429993, 429993, 429993, 429993, 158585, 158585, 158585, 158585, 158585, 173289, 173289, 173289, 173289, 173289, 169434, 169434, 169434, 169434, 169434, 177730, 291359, 291359, 291359, 291359, 291359, 242085, 242085, 242085, 242085, 220130, 220130, 220130, 220130, 220130, 269473, 269473, 269473, 269473, 269473, 295802, 295802, 295802, 295802, 203395, 203395, 203395, 203395, 203395, N/A, N/A, N/A, N/A, 160510, 160510, 160510, 160510, 160510, 169434, 169434, 169434, 169434, 169434, 241772, 241772, 241772, 241772, 241772, 445972, 445972, 445972, 445972, 445972, 475457, 475457, 475457, 475457, 475457, 160594, 160594, 160594, 160594, 160594, 158131, 158131, 158131, N/A, N/A, N/A, N/A, N/A, 174715, 174715, 174715, 174715, 174715, N/A, 577820, 172089, 172089, 172089, 172089, 172089, 200710, 200710, 200710, 200710, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, 577820, 577820, 158131";
////        String blinkIds4 = "158131, N/A, N/A, N/A, N/A, N/A, 473401, 473401, 473401, 473401, 473401, 228241, 228241, 228241, 228241, 228241, 584941, 584941, 584941, 584941, 473400, 473400, 473400, 473400, 473400, 290511, 290511, 290511, 290511, 290511, 582584, 400078, 400078, 400078, N/A, N/A, N/A, N/A, 594631, 594631, 594631, 553143, 553143, 551033, 551033, N/A, N/A, 274650, 274650, 274650, 274650, 274650, 584941, 583258, 583258, 583258, 583258, 594632, 582584, 582584, 582584, 400078, N/A, 594631, N/A, N/A, 551033, 551033, 551033, N/A, N/A, 583258, 594632, 594632, N/A, N/A, 207454, 286047, 286047, 286047, 286047, 286047, 286939, N/A, 244899, 269382, 204885, 173420, 259872, N/A, 577820, 582584, 400078, 594631, N/A, 594632, 170033, 258949, 184161, 177730, 262942, 295802, N/A, 200710, N/A, N/A, 577820, N/A, 594632, N/A, N/A, N/A";
//
//           String blinkIds= "NA, 207454, 207454, 207454, 207454, 202624, 235013, 235013, 235013, 235013, 235013, 202624, 202624, 202624, 202624, NA, 296359, 296359, 296359, 296359, 296359, 176094, 176094, 176094, 176094, 176094, 222717, 222717, 222717, 222717, 287344, NA, NA, NA, 182198, 182198, 182198, 182198, 182198, 237854, 237854, 183474, 183474, 453501, 453501, 453501, 453501, 453501, 170033, 170033, 170033, 170033, 237854, 237854, 237854, 474324, 474324, 474324, 474324, 474324, 258949, 258949, 258949, 258949, 153566, 153566, 153566, 153566, 153566, 285229, 285229, 285229, 285229, 285229, 183474, 183474, 183474, 287344, 287344, 287344, 287344, 241223, 241223, 241223, 241223, 241223, 229409, 229409, 229409, 229409, 229409, 154892, 154892, 154892, 154892, 154892, 249335, 249335, 249335, 249335, 249335, 237361, 237361, 237361, 237361, 237361, 237361, 237361, 237361, 237361, 237361, 155744, 155744, 155744, 155744, 155744, 168238, 168238, 168238, 168238, 168238, 175833, 175833, 175833, 175833, 175833, 159437, 159437, 159437, 159437, 159437, 213323, 269382, 269382, 269382, 269382, 175283, 175283, 175283, 175283, 204885, 204885, 204885, 204885, 244899, 244899, 244899, 244899, 175283, 272021, 272021, 272021, 272021, 272021, 453503, 453503, 453503, 453503, 453503, 213323, 213323, 213323, 213323, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 265524, 187550, 187550, 187550, 187550, 187550, 205571, 205571, 205571, 205571, 205571, 163181, 163181, 163181, 163181, 163181, 150773, 150773, 150773, 150773, 150773, 453953, 453953, 453953, 453953, 453953, 217047, 217047, 217047, 217047, 217047, 287768, 287768, 287768, 287768, 287768, 173420, 173420, 173420, 269872, 269872, 269872, 269872, 173420, 199432, 199432, 199432, 199432, 199432, 204513, 204513, 204513, 204513, 204513, 184161, 184161, 184161, 184161, 177730, 177730, 177730, 167106, 167106, 167106, 167106, 167106, 238235, 238235, 238235, 238235, 238235, 218840, 218840, 218840, 218840, 218840, 193624, 193624, 193624, 193624, 193624, 429993, 429993, 429993, 429993, 429993, 158585, 158585, 158585, 158585, 158585, 173289, 173289, 173289, 173289, 173289, 169434, 169434, 169434, 169434, 169434, 177730, 291359, 291359, 291359, 291359, 291359, 242085, 242085, 242085, 242085, 220130, 220130, 220130, 220130, 220130, 269473, 269473, 269473, 269473, 269473, 295802, 295802, 295802, 295802, 203395, 203395, 203395, 203395, 203395, N/A, N/A, N/A, N/A, 160510, 160510, 160510, 160510, 160510, 169434, 169434, 169434, 169434, 169434, 241772, 241772, 241772, 241772, 241772, 445972, 445972, 445972, 445972, 445972, 475457, 475457, 475457, 475457, 475457, 160594, 160594, 160594, 160594, 160594, 158131, 158131, 158131, N/A, N/A, N/A, N/A, N/A, 174715, 174715, 174715, 174715, 174715, N/A, 577820, 172089, 172089, 172089, 172089, 172089, 200710, 200710, 200710, 200710, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, N/A, 577820, 577820, 158131, 158131, N/A, N/A, N/A, N/A, N/A, 473401, 473401, 473401, 473401, 473401, 228241, 228241, 228241, 228241, 228241, 584941, 584941, 584941, 584941, 473400, 473400, 473400, 473400, 473400, 290511, 290511, 290511, 290511, 290511, 582584, 400078, 400078, 400078, N/A, N/A, N/A, N/A, 594631, 594631, 594631, 553143, 553143, 551033, 551033, N/A, N/A, 274650, 274650, 274650, 274650, 274650, 584941, 583258, 583258, 583258, 583258, 594632, 582584, 582584, 582584, 400078, N/A, 594631, N/A, N/A, 551033, 551033, 551033, N/A, N/A, 583258, 594632, 594632, N/A, N/A, 207454, 286047, 286047, 286047, 286047, 286047, 286939, N/A, 244899, 269382, 204885, 173420, 259872, N/A, 577820, 582584, 400078, 594631, N/A, 594632, 170033, 258949, 184161, 177730, 262942, 295802, N/A, 200710, N/A, N/A, 577820, N/A, 594632, N/A, N/A, N/A";
//        List<String> blink_ids = Arrays.asList(blinkIds.split(", "));
//        if(drug_ids.size() == blink_ids.size()) {
//            for (int i = 0; i < drug_ids.size(); i++) {
//                try {
//                    int drug_id = Integer.parseInt(drug_ids.get(i));
//                    DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drug_id, 5).get(0);
//                    if (!blink_ids.get(i).equals("NA")) {
//                        drugRequest.setGood_rx_id(blink_ids.get(i));
//                        drugRequestRepository.save(drugRequest);
//                    }
//                }catch (Exception ex){
//                    System.out.println(drug_ids.get(i));
//                }
//            }
//        }else{
//            System.out.println("NOT SAME SIZE");
//            System.out.println(drug_ids.size());
//            System.out.println(blink_ids.size());
//        }
//    }
}