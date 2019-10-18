package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



@CrossOrigin
@RestController
public class DrugRequestController {

    @Autowired
    DrugRequestRepository drugRequestRepository;

    @Autowired
    DrugMasterRepository drugMasterRepository;
    Gson gson =new Gson();

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

    @GetMapping("/bulk/drug/request")
    public void createBulkDrugRequests(){
        String[] ndcArr = new String[]{};
        String[] goodRxIdArr = new String[]{};
        List<String> ndcList = Arrays.asList(ndcArr);
        List<String> goodRxIdList = Arrays.asList(goodRxIdArr);
        DrugReqList drugReqList =gson.fromJson("{\"drugReqList\":[{\"drugGSN\":005039,\"drugName\":\"ALBUTEROL SULFATE\",\"dosageStrength\":\"0.63 MG/3 ML \",\"drugForm\":\"SOLUTION\",\"quantity\":360,\"brandIndicator\":\"GENERIC\",\"goodRxId\":35123,\"ndc\":00378699152, \"blinkHealthId\":\"N/A\"},\n" +
                "{\"drugGSN\":002536,\"drugName\":\"ALLOPURINOL\",\"dosageStrength\":\"300 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":55,\"ndc\":00378018101, \"blinkHealthId\":\"266145\"},\n" +
                "{\"drugGSN\":002535,\"drugName\":\"ALLOPURINOL\",\"dosageStrength\":\"100 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":11,\"ndc\":00378013701,\"blinkHealthId\":\"203487\"},\n" +
                "{\"drugGSN\":042683,\"drugName\":\"AMOXICILLIN TRIHYDRATE\",\"dosageStrength\":\"400 MG/5 ML\",\"drugForm\":\"SUSP RECON\",\"quantity\":100,\"brandIndicator\":\"GENERIC\",\"goodRxId\":36555,\"ndc\":00093416173, \"blinkHealthId\":\"268912\"},\n" +
                "{\"drugGSN\":040292,\"drugName\":\"AMOXICILLIN TRIHYDRATE\",\"dosageStrength\":\"875 MG\",\"drugForm\":\"TABLET\",\"quantity\":20,\"brandIndicator\":\"GENERIC\",\"goodRxId\":12523,\"ndc\":00093226401, \"blinkHealthId\":\"222079\"},\n" +
                "{\"drugGSN\":016995,\"drugName\":\"ASPIRIN\",\"dosageStrength\":\"81 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":1159,\"ndc\":00113027468, \"blinkHealthId\":\"450873\"},\n" +
                "{\"drugGSN\":005139,\"drugName\":\"ATENOLOL\",\"dosageStrength\":\"50 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":139,\"ndc\":00113027468, \"blinkHealthId\":\"290337\"},\n" +
                "{\"drugGSN\":015864,\"drugName\":\"ATENOLOL\",\"dosageStrength\":\"25 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":138,\"ndc\":00093078701, \"blinkHealthId\":\"192407\"},\n" +
                "{\"drugGSN\":029967,\"drugName\":\"ATORVASTATIN CALCIUM\",\"dosageStrength\":\"10 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":33395,\"ndc\":00093505698, \"blinkHealthId\":\"170427\"},\n" +
                "{\"drugGSN\":045855,\"drugName\":\"ATOVAQUONE/PROGUANIL HYDROCHLORIDE\",\"dosageStrength\":\"250 mg-100 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":33412,\"ndc\":00378416201, \"blinkHealthId\":\"281830\"},\n" +
                "{\"drugGSN\":022624,\"drugName\":\"AZITHROMYCIN\",\"dosageStrength\":\"500 MG\",\"drugForm\":\"TABLET\",\"quantity\":3,\"brandIndicator\":\"GENERIC\",\"goodRxId\":33315,\"ndc\":00093716956, \"blinkHealthId\":\"445655\"},\n" +
                "{\"drugGSN\":004641,\"drugName\":\"BENZONATATE\",\"dosageStrength\":\"100 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":90,\"brandIndicator\":\"GENERIC\",\"goodRxId\":22,\"ndc\":00904656460, \"blinkHealthId\":\"235305\"},\n" +
                "{\"drugGSN\":044168,\"drugName\":\"BENZONATATE\",\"dosageStrength\":\"200 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":1114,\"ndc\":51224000150, \"blinkHealthId\":\"175383\"},\n" +
                "{\"drugGSN\":053006,\"drugName\":\"BUPROPION HYDROCHLORIDE XL\",\"dosageStrength\":\"150 MG/24 hours\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":27522,\"ndc\":00115681108, \"blinkHealthId\":\"451598\"},\n" +
                "{\"drugGSN\":053007,\"drugName\":\"BUPROPION HYDROCHLORIDE XL\",\"dosageStrength\":\"300 MG/24 hours\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":27523,\"ndc\":00378200905, \"blinkHealthId\":\"451597\"},\n" +
                "{\"drugGSN\":019293,\"drugName\":\"CARVEDILOL\",\"dosageStrength\":\"25 MG\",\"drugForm\":\"TABLET\",\"quantity\":60,\"brandIndicator\":\"GENERIC\",\"goodRxId\":157,\"ndc\":00093729601, \"blinkHealthId\":\"158839\"},\n" +
                "{\"drugGSN\":040257,\"drugName\":\"CEFDINIR\",\"dosageStrength\":\"300 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":60,\"brandIndicator\":\"GENERIC\",\"goodRxId\":4170,\"ndc\":00093316006, \"blinkHealthId\":\"285453\"},\n" +
                "{\"drugGSN\":017037,\"drugName\":\"CETIRIZINE HYDROCHLORIDE\",\"dosageStrength\":\"10 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":7449,\"ndc\":00378363701, \"blinkHealthId\":\"170282\"},\n" +
                "{\"drugGSN\":057959,\"drugName\":\"CHLORHEXIDINE GLUCONATE\",\"dosageStrength\":\"0.12%\",\"drugForm\":\"MOUTHWASH\",\"quantity\":473,\"brandIndicator\":\"GENERIC\",\"goodRxId\":44233,\"ndc\":00116200104, \"blinkHealthId\":\"475303\"},\n" +
                "{\"drugGSN\":002329,\"drugName\":\"CYANOCOBALAMIN\",\"dosageStrength\":\"1000 MCG/ML\",\"drugForm\":\"VIAL\",\"quantity\":1,\"brandIndicator\":\"GENERIC\",\"goodRxId\":31318,\"ndc\":00143961910, \"blinkHealthId\":\"238818\"},\n" +
                "{\"drugGSN\":047478,\"drugName\":\"CYCLOBENZAPRINE HYDROCHLORIDE\",\"dosageStrength\":\"5 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":57,\"ndc\":00378077101, \"blinkHealthId\":\"448725\"},\n" +
                "{\"drugGSN\":008374,\"drugName\":\"DICLOFENAC SODIUM\",\"dosageStrength\":\"75 MG\",\"drugForm\":\"TABLET\",\"quantity\":60,\"brandIndicator\":\"GENERIC\",\"goodRxId\":29889,\"ndc\":00228255106, \"blinkHealthId\":\"263280\"},\n" +
                "{\"drugGSN\":009218,\"drugName\":\"DOXYCYCLINE HYCLATE\",\"dosageStrength\":\"100 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":20,\"brandIndicator\":\"GENERIC\",\"goodRxId\":44,\"ndc\":00143211250, \"blinkHealthId\":\"162562\"},\n" +
                "{\"drugGSN\":015943,\"drugName\":\"DOXYCYCLINE MONOHYDRATE\",\"dosageStrength\":\"100 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":20,\"brandIndicator\":\"GENERIC\",\"goodRxId\":36034,\"ndc\":00378602389, \"blinkHealthId\":\"235975\"},\n" +
                "{\"drugGSN\":057892,\"drugName\":\"DULOXETINE HYDROCHLORIDE\",\"dosageStrength\":\"30 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":39015,\"ndc\":00093754356, \"blinkHealthId\":\"471239\"},\n" +
                "{\"drugGSN\":057893,\"drugName\":\"DULOXETINE HYDROCHLORIDE\",\"dosageStrength\":\"60 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":39016,\"ndc\":00093754456, \"blinkHealthId\":\"471240\"},\n" +
                "{\"drugGSN\":050760,\"drugName\":\"ESCITALOPRAM OXALATE\",\"dosageStrength\":\"20 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":33932,\"ndc\":00093585201, \"blinkHealthId\":\"445973\"},\n" +
                "{\"drugGSN\":001645,\"drugName\":\"FERROUS SULFATE\",\"dosageStrength\":\"325 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":13300,\"ndc\":00245010801, \"blinkHealthId\":\"475308\"},\n" +
                "{\"drugGSN\":046215,\"drugName\":\"FLUOXETINE HYDROCHLORIDE\",\"dosageStrength\":\"40 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":247,\"ndc\":62332002431, \"blinkHealthId\":\"151828\"},\n" +
                "{\"drugGSN\":046213,\"drugName\":\"FLUOXETINE HYDROCHLORIDE\",\"dosageStrength\":\"10 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":28088,\"ndc\":65862019201, \"blinkHealthId\":\"255189\"},\n" +
                "{\"drugGSN\":002366,\"drugName\":\"FOLIC ACID\",\"dosageStrength\":\"1 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":1233,\"ndc\":00591521601, \"blinkHealthId\":\"264890\"},\n" +
                "{\"drugGSN\":008208,\"drugName\":\"FUROSEMIDE\",\"dosageStrength\":\"20 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":175,\"ndc\":69315011610, \"blinkHealthId\":\"270143\"},\n" +
                "{\"drugGSN\":008209,\"drugName\":\"FUROSEMIDE\",\"dosageStrength\":\"40 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":176,\"ndc\":00054429925, \"blinkHealthId\":\"152894\"},\n" +
                "{\"drugGSN\":021413,\"drugName\":\"GABAPENTIN\",\"dosageStrength\":\"100 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":90,\"brandIndicator\":\"GENERIC\",\"goodRxId\":812,\"ndc\":69097081312, \"blinkHealthId\":\"271154\"},\n" +
                "{\"drugGSN\":028915,\"drugName\":\"HYDROCHLOROTHIAZIDE\",\"dosageStrength\":\"12.5 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":29962,\"ndc\":16729018201, \"blinkHealthId\":\"549792\"},\n" +
                "{\"drugGSN\":003728,\"drugName\":\"HYDROXYZINE HYDROCHLORIDE\",\"dosageStrength\":\"25 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":29979,\"ndc\":00093506101, \"blinkHealthId\":\"265723\"},\n" +
                "{\"drugGSN\":008349,\"drugName\":\"IBUPROFEN\",\"dosageStrength\":\"600 MG\",\"drugForm\":\"TABLET\",\"quantity\":90,\"brandIndicator\":\"GENERIC\",\"goodRxId\":65,\"ndc\":00904585440, \"blinkHealthId\":\"275877\"},\n" +
                "{\"drugGSN\":044633,\"drugName\":\"LEVETIRACETAM\",\"dosageStrength\":\"500 MG\",\"drugForm\":\"TABLET\",\"quantity\":60,\"brandIndicator\":\"GENERIC\",\"goodRxId\":1528,\"ndc\":68180011316, \"blinkHealthId\":\"176050\"},\n" +
                "{\"drugGSN\":029928,\"drugName\":\"LEVOFLOXACIN\",\"dosageStrength\":\"500 MG\",\"drugForm\":\"TABLET\",\"quantity\":10,\"brandIndicator\":\"GENERIC\",\"goodRxId\":30002,\"ndc\":00781579101, \"blinkHealthId\":\"277505\"},\n" +
                "{\"drugGSN\":030986,\"drugName\":\"LEVONORGESTREL-ETH ESTRA\",\"dosageStrength\":\"20 mcg-100 mcg\",\"drugForm\":\"TABLET\",\"quantity\":28,\"brandIndicator\":\"GENERIC\",\"goodRxId\":43204,\"ndc\":00378728753, \"blinkHealthId\":\"N/A\"},\n" +
                "{\"drugGSN\":006648,\"drugName\":\"LEVOTHYROXINE\",\"dosageStrength\":\"25 MCG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":28419,\"ndc\":00378180010, \"blinkHealthId\":\"210792\"},\n" +
                "{\"drugGSN\":006652,\"drugName\":\"LEVOTHYROXINE SODIUM\",\"dosageStrength\":\"112 MCG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":31929,\"ndc\":00378181110, \"blinkHealthId\":\"296237\"},\n" +
                "{\"drugGSN\":006654,\"drugName\":\"LEVOTHYROXINE SODIUM\",\"dosageStrength\":\"150 MCG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":28416,\"ndc\":00378181510, \"blinkHealthId\":\"248321\"},\n" +
                "{\"drugGSN\":000393,\"drugName\":\"LISINOPRIL\",\"dosageStrength\":\"5 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":191,\"ndc\":00143971501, \"blinkHealthId\":\"268801\"},\n" +
                "{\"drugGSN\":000389,\"drugName\":\"LISINOPRIL/HYDROCHLOROTHIAZIDE\",\"dosageStrength\":\"25 mg-20 mg\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":13522,\"ndc\":00143126401, \"blinkHealthId\":\"261128\"},\n" +
                "{\"drugGSN\":000388,\"drugName\":\"LISINOPRIL/HYDROCHLOROTHIAZIDE\",\"dosageStrength\":\"12.5 MG-20 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":13521,\"ndc\":00143126301, \"blinkHealthId\":\"171884\"},\n" +
                "{\"drugGSN\":021277,\"drugName\":\"LISINOPRIL/HYDROCHLOROTHIAZIDE\",\"dosageStrength\":\"12.5 MG-10 mg\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":13523,\"ndc\":00143126201, \"blinkHealthId\":\"275733\"},\n" +
                "{\"drugGSN\":018698,\"drugName\":\"LORATADINE\",\"dosageStrength\":\"10 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":25,\"ndc\":00067067430, \"blinkHealthId\":\"292857\"},\n" +
                "{\"drugGSN\":023465,\"drugName\":\"LOSARTAN/HCTZ\",\"dosageStrength\":\"12.5 MG-50 mg\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":22723,\"ndc\":00093736710, \"blinkHealthId\":\"280204\"},\n" +
                "{\"drugGSN\":029156,\"drugName\":\"MELOXICAM\",\"dosageStrength\":\"7.5 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":68,\"ndc\":29300012401, \"blinkHealthId\":\"216915\"},\n" +
                "{\"drugGSN\":046754,\"drugName\":\"METFORMIN HYDROCHLORIDE\",\"dosageStrength\":\"500 MG\",\"drugForm\":\"TABLET\",\"quantity\":60,\"brandIndicator\":\"GENERIC\",\"goodRxId\":96,\"ndc\":00093104810, \"blinkHealthId\":\"155744\"},\n" +
                "{\"drugGSN\":004655,\"drugName\":\"METHOCARBAMOL\",\"dosageStrength\":\"750 MG\",\"drugForm\":\"TABLET\",\"quantity\":90,\"brandIndicator\":\"GENERIC\",\"goodRxId\":5048,\"ndc\":00143129201, \"blinkHealthId\":\"436963\"},\n" +
                "{\"drugGSN\":016599,\"drugName\":\"METOPROLOL SUCCINATE ER\",\"dosageStrength\":\"50 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":30053,\"ndc\":00904632361, \"blinkHealthId\":\"175686\"},\n" +
                "{\"drugGSN\":005132,\"drugName\":\"METOPROLOL TARTRATE\",\"dosageStrength\":\"50 MG\",\"drugForm\":\"TABLET\",\"quantity\":60,\"brandIndicator\":\"GENERIC\",\"goodRxId\":1569,\"ndc\":00378003201, \"blinkHealthId\":\"251929\"},\n" +
                "{\"drugGSN\":008362,\"drugName\":\"NAPROXEN\",\"dosageStrength\":\"500 MG\",\"drugForm\":\"TABLET\",\"quantity\":60,\"brandIndicator\":\"GENERIC\",\"goodRxId\":71,\"ndc\":49483061801, \"blinkHealthId\":\"293894\"},\n" +
                "{\"drugGSN\":003301,\"drugName\":\"NORETH A-ET ESTRA/FE FUMARATE\",\"dosageStrength\":\"20 mcg-1 mg\",\"drugForm\":\"TABLET\",\"quantity\":28,\"brandIndicator\":\"GENERIC\",\"goodRxId\":DNE,\"ndc\":00378728353, \"blinkHealthId\":\"N/A\"},\n" +
                "{\"drugGSN\":041562,\"drugName\":\"ONDANSETRON  ODT\",\"dosageStrength\":\"4 MG\",\"drugForm\":\"RAPDIS\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":30105,\"ndc\":00173056900, \"blinkHealthId\":\"285288\"},\n" +
                "{\"drugGSN\":046223,\"drugName\":\"PAROXETINE HYDROCHLORIDE\",\"dosageStrength\":\"20 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":28813,\"ndc\":00378700210, \"blinkHealthId\":\"237883\"},\n" +
                "{\"drugGSN\":009478,\"drugName\":\"PHENAZOPYRIDINE HYDROCHLORIDE\",\"dosageStrength\":\"200 MG\",\"drugForm\":\"TABLET\",\"quantity\":6,\"brandIndicator\":\"GENERIC\",\"goodRxId\":8163,\"ndc\":10135040014, \"blinkHealthId\":\"190783\"},\n" +
                "{\"drugGSN\":022346,\"drugName\":\"POTASSIUM CHLORIDE\",\"dosageStrength\":\"20 MEQ/15 mL\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":2098,\"ndc\":00603154258, \"blinkHealthId\":\"223280\"},\n" +
                "{\"drugGSN\":006753,\"drugName\":\"PREDNISONE\",\"dosageStrength\":\"5 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":12812,\"ndc\":00054472831, \"blinkHealthId\":\"187978\"},\n" +
                "{\"drugGSN\":040941,\"drugName\":\"RABEPRAZOLE SODIUM\",\"dosageStrength\":\"20 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":38953,\"ndc\":00093006456, \"blinkHealthId\":\"161355\"},\n" +
                "{\"drugGSN\":011673,\"drugName\":\"RANITIDINE HYDROCHLORIDE\",\"dosageStrength\":\"150 MG\",\"drugForm\":\"TABLET\",\"quantity\":60,\"brandIndicator\":\"GENERIC\",\"goodRxId\":120,\"ndc\":68462024805, \"blinkHealthId\":\"242904\"},\n" +
                "{\"drugGSN\":046227,\"drugName\":\"SERTRALINE HYDROCHLORIDE\",\"dosageStrength\":\"25 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":822,\"ndc\":00143965609, \"blinkHealthId\":\"287768\"},\n" +
                "{\"drugGSN\":039189,\"drugName\":\"SILDENAFIL CITRATE\",\"dosageStrength\":\"25 MG\",\"drugForm\":\"TABLET\",\"quantity\":6,\"brandIndicator\":\"GENERIC\",\"goodRxId\":43015,\"ndc\":59762003401, \"blinkHealthId\":\"205275\"},\n" +
                "{\"drugGSN\":016578,\"drugName\":\"SIMVASTATIN\",\"dosageStrength\":\"20 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":5431,\"ndc\":00093715431, \"blinkHealthId\":\"238251\"},\n" +
                "{\"drugGSN\":016579,\"drugName\":\"SIMVASTATIN\",\"dosageStrength\":\"40 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":1611,\"ndc\":00093715510, \"blinkHealthId\":\"209275\"},\n" +
                "{\"drugGSN\":006817,\"drugName\":\"SPIRONOLACTONE\",\"dosageStrength\":\"25 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":209,\"ndc\":00228280311, \"blinkHealthId\":\"208244\"},\n" +
                "{\"drugGSN\":006818,\"drugName\":\"SPIRONOLACTONE\",\"dosageStrength\":\"50 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":731,\"ndc\":00228267211, \"blinkHealthId\":\"294909\"},\n" +
                "{\"drugGSN\":030274,\"drugName\":\"TIZANIDINE HYDROCHLORIDE\",\"dosageStrength\":\"4 MG\",\"drugForm\":\"TABLET\",\"quantity\":90,\"brandIndicator\":\"GENERIC\",\"goodRxId\":29747,\"ndc\":00185440010, \"blinkHealthId\":\"175236\"},\n" +
                "{\"drugGSN\":046242,\"drugName\":\"TRAZODONE HYDROCHLORIDE\",\"dosageStrength\":\"100 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":263,\"ndc\":50111043403, \"blinkHealthId\":\"230585\"},\n" +
                "{\"drugGSN\":007594,\"drugName\":\"TRIAMCINOLONE ACETONIDE TOPICAL\",\"dosageStrength\":\"0.1% Cream\",\"drugForm\":\"CREAM\",\"quantity\":454,\"brandIndicator\":\"GENERIC\",\"goodRxId\":35054,\"ndc\":45802006436, \"blinkHealthId\":\"207475\"},\n" +
                "{\"drugGSN\":023989,\"drugName\":\"VALACYCLOVIR\",\"dosageStrength\":\"500 MG\",\"drugForm\":\"TABLET\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":8104,\"ndc\":00093725856, \"blinkHealthId\":\"204203\"},\n" +
                "{\"drugGSN\":046405,\"drugName\":\"VENLAFAXINE HYDROCHLORIDE ER\",\"dosageStrength\":\"150 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":30,\"brandIndicator\":\"GENERIC\",\"goodRxId\":29462,\"ndc\":00093738605, \"blinkHealthId\":\"193691\"},\n" +
                "{\"drugGSN\":045100,\"drugName\":\"ZONISAMIDE\",\"dosageStrength\":\"100 MG\",\"drugForm\":\"CAPSULE\",\"quantity\":60,\"brandIndicator\":\"GENERIC\",\"goodRxId\":29638,\"ndc\":68462013001, \"blinkHealthId\":\"216127\"}]}\n", DrugReqList.class);
        for (int i =0;i<drugReqList.drugReqList.size();i++) {
            try {
                DrugMaster drugMaster = drugMasterRepository.findAllByNDC(drugReqList.drugReqList.get(i).ndc).get(0);

                drugMasterToInsideRxRequest(drugMaster);
                drugMasterToUSPharmRequest(drugMaster);
                drugMasterToWellRxRequest(drugMaster);
                drugMasterToMedImpactRequest(drugMaster);
                drugMasterToSingleCareRequest(drugMaster);
                drugMasterToBlinkRequest(drugMaster, drugReqList.drugReqList.get(i).blinkHealthId);
                drugMasterToGoodRxRequest(drugMaster, drugReqList.drugReqList.get(i).goodRxId + "");
            }catch (Exception ex){
                ex.printStackTrace();
                System.out.println("FAILED");
                System.out.println(drugReqList.drugReqList.get(i).ndc);
            }
        }
        System.out.println("DONE");
    }

    public void drugMasterToInsideRxRequest(DrugMaster drugMaster){
        DrugRequest drugRequest = new DrugRequest();
        drugRequest.setProgramId(0);
        if(drugMaster.getZipCode().equals("90036")){
            drugRequest.setLongitude("-118.3520389");
            drugRequest.setLatitude("34.0664817");
        }
        else if(drugMaster.getZipCode().equals("30606")){
            drugRequest.setLongitude("-83.4323375");
            drugRequest.setLatitude("33.9448436");
        }
        else if(drugMaster.getZipCode().equals("60639")){
            drugRequest.setLongitude("-87.7517295");
            drugRequest.setLatitude("41.9225138");
        }
        else if(drugMaster.getZipCode().equals("10023")){
            drugRequest.setLongitude("-73.9800645");
            drugRequest.setLatitude("40.7769059");
        }
        else if(drugMaster.getZipCode().equals("75034")){
            drugRequest.setLongitude("-96.8565427");
            drugRequest.setLatitude("33.1376528");
        }
        drugRequest.setDrugName(drugMaster.getName());
        drugRequest.setDrugId(drugMaster.getId()+"");

        if(drugMaster.getDrugType().equals("B")){
            drugRequest.setBrandIndicator("BRAND_WITH_GENERIC");
        }else if(drugMaster.getDrugType().equals("G")){
            drugRequest.setBrandIndicator("GENERIC");
        }else{
            drugRequest.setBrandIndicator(drugMaster.getDrugType());
        }
        drugRequest.setNdc(drugMaster.getNdc());
        drugRequest.setQuantity(drugMaster.getQuantity()+"");
        drugRequest.setZipcode(drugMaster.getZipCode());

        drugRequestRepository.save(drugRequest);
    }
    public void drugMasterToUSPharmRequest(DrugMaster drugMaster){
        DrugRequest drugRequest = new DrugRequest();
        drugRequest.setProgramId(1);
        if(drugMaster.getZipCode().equals("90036")){
            drugRequest.setLongitude("-118.3520389");
            drugRequest.setLatitude("34.0664817");
        }
        else if(drugMaster.getZipCode().equals("30606")){
            drugRequest.setLongitude("-83.4323375");
            drugRequest.setLatitude("33.9448436");
        }
        else if(drugMaster.getZipCode().equals("60639")){
            drugRequest.setLongitude("-87.7517295");
            drugRequest.setLatitude("41.9225138");
        }
        else if(drugMaster.getZipCode().equals("10023")){
            drugRequest.setLongitude("-73.9800645");
            drugRequest.setLatitude("40.7769059");
        }
        else if(drugMaster.getZipCode().equals("75034")){
            drugRequest.setLongitude("-96.8565427");
            drugRequest.setLatitude("33.1376528");
        }
        drugRequest.setDrugName(drugMaster.getName());
        drugRequest.setDrugId(drugMaster.getId()+"");

        if(drugMaster.getZipCode().equals("B")){
            drugRequest.setBrandIndicator("BRAND_WITH_GENERIC");
        }else if(drugMaster.getDrugType().equals("G")){
            drugRequest.setBrandIndicator("GENERIC");
        }else{
            drugRequest.setBrandIndicator(drugMaster.getDrugType());
        }
        drugRequest.setNdc(drugMaster.getNdc());
        drugRequest.setQuantity(drugMaster.getQuantity()+"");
        drugRequest.setZipcode(drugMaster.getZipCode());

        drugRequestRepository.save(drugRequest);
    }
    public void drugMasterToWellRxRequest(DrugMaster drugMaster){
        DrugRequest drugRequest = new DrugRequest();
        drugRequest.setProgramId(2);
        if(drugMaster.getZipCode().equals("90036")){
            drugRequest.setLongitude("-118.3520389");
            drugRequest.setLatitude("34.0664817");
        }
        else if(drugMaster.getZipCode().equals("30606")){
            drugRequest.setLongitude("-83.4323375");
            drugRequest.setLatitude("33.9448436");
        }
        else if(drugMaster.getZipCode().equals("60639")){
            drugRequest.setLongitude("-87.7517295");
            drugRequest.setLatitude("41.9225138");
        }
        else if(drugMaster.getZipCode().equals("10023")){
            drugRequest.setLongitude("-73.9800645");
            drugRequest.setLatitude("40.7769059");
        }
        else if(drugMaster.getZipCode().equals("75034")){
            drugRequest.setLongitude("-96.8565427");
            drugRequest.setLatitude("33.1376528");
        }
        drugRequest.setDrugName(drugMaster.getName());
        drugRequest.setDrugId(drugMaster.getId()+"");

        if(drugMaster.getDrugType().equals("BRAND_WITH_GENERIC")){
            drugRequest.setBrandIndicator("B");
        }else if(drugMaster.getDrugType().equals("GENERIC")){
            drugRequest.setBrandIndicator("G");
        }else{
            drugRequest.setBrandIndicator(drugMaster.getDrugType());
        }
        drugRequest.setQuantity(drugMaster.getQuantity()+"");
        drugRequest.setZipcode(drugMaster.getZipCode());
        drugRequest.setGsn(drugMaster.getGsn());
        drugRequestRepository.save(drugRequest);
    }
    public void drugMasterToMedImpactRequest(DrugMaster drugMaster){
        DrugRequest drugRequest = new DrugRequest();
        drugRequest.setProgramId(3);
        if(drugMaster.getZipCode().equals("90036")){
            drugRequest.setLongitude("-118.3520389");
            drugRequest.setLatitude("34.0664817");
        }
        else if(drugMaster.getZipCode().equals("30606")){
            drugRequest.setLongitude("-83.4323375");
            drugRequest.setLatitude("33.9448436");
        }
        else if(drugMaster.getZipCode().equals("60639")){
            drugRequest.setLongitude("-87.7517295");
            drugRequest.setLatitude("41.9225138");
        }
        else if(drugMaster.getZipCode().equals("10023")){
            drugRequest.setLongitude("-73.9800645");
            drugRequest.setLatitude("40.7769059");
        }
        else if(drugMaster.getZipCode().equals("75034")){
            drugRequest.setLongitude("-96.8565427");
            drugRequest.setLatitude("33.1376528");
        }
        drugRequest.setDrugName(drugMaster.getName());
        drugRequest.setDrugId(drugMaster.getId()+"");

        if(drugMaster.getDrugType().equals("BRAND_WITH_GENERIC")){
            drugRequest.setBrandIndicator("B");
        }else if(drugMaster.getDrugType().equals("GENERIC")){
            drugRequest.setBrandIndicator("G");
        }else{
            drugRequest.setBrandIndicator(drugMaster.getDrugType());
        }
        drugRequest.setQuantity(drugMaster.getQuantity()+"");
        drugRequest.setZipcode(drugMaster.getZipCode());
        drugRequest.setGsn(drugMaster.getGsn());
        drugRequestRepository.save(drugRequest);
    }
    public void drugMasterToSingleCareRequest(DrugMaster drugMaster){
        DrugRequest drugRequest = new DrugRequest();
        drugRequest.setProgramId(4);
        if(drugMaster.getZipCode().equals("90036")){
            drugRequest.setLongitude("-118.3520389");
            drugRequest.setLatitude("34.0664817");
        }
        else if(drugMaster.getZipCode().equals("30606")){
            drugRequest.setLongitude("-83.4323375");
            drugRequest.setLatitude("33.9448436");
        }
        else if(drugMaster.getZipCode().equals("60639")){
            drugRequest.setLongitude("-87.7517295");
            drugRequest.setLatitude("41.9225138");
        }
        else if(drugMaster.getZipCode().equals("10023")){
            drugRequest.setLongitude("-73.9800645");
            drugRequest.setLatitude("40.7769059");
        }
        else if(drugMaster.getZipCode().equals("75034")){
            drugRequest.setLongitude("-96.8565427");
            drugRequest.setLatitude("33.1376528");
        }
        drugRequest.setDrugName(drugMaster.getName());
        drugRequest.setDrugId(drugMaster.getId()+"");

        drugRequest.setNdc(drugMaster.getNdc());
        drugRequest.setQuantity(drugMaster.getQuantity()+"");
        drugRequest.setZipcode(drugMaster.getZipCode());

        drugRequestRepository.save(drugRequest);
    }
    public void drugMasterToBlinkRequest(DrugMaster drugMaster, String blinkId){
        DrugRequest drugRequest = new DrugRequest();
        drugRequest.setProgramId(5);
        if(drugMaster.getZipCode().equals("90036")){
            drugRequest.setLongitude("-118.3520389");
            drugRequest.setLatitude("34.0664817");
        }
        else if(drugMaster.getZipCode().equals("30606")){
            drugRequest.setLongitude("-83.4323375");
            drugRequest.setLatitude("33.9448436");
        }
        else if(drugMaster.getZipCode().equals("60639")){
            drugRequest.setLongitude("-87.7517295");
            drugRequest.setLatitude("41.9225138");
        }
        else if(drugMaster.getZipCode().equals("10023")){
            drugRequest.setLongitude("-73.9800645");
            drugRequest.setLatitude("40.7769059");
        }
        else if(drugMaster.getZipCode().equals("75034")){
            drugRequest.setLongitude("-96.8565427");
            drugRequest.setLatitude("33.1376528");
        }
        drugRequest.setDrugName(drugMaster.getName());
        drugRequest.setDrugId(drugMaster.getId()+"");

        if(drugMaster.getDrugType().equals("B")){
            drugRequest.setBrandIndicator("BRAND_WITH_GENERIC");
        }else if(drugMaster.getDrugType().equals("G")){
            drugRequest.setBrandIndicator("GENERIC");
        }else{
            drugRequest.setBrandIndicator(drugMaster.getDrugType());
        }
        drugRequest.setNdc(drugMaster.getNdc());
        drugRequest.setQuantity(drugMaster.getQuantity()+"");
        drugRequest.setZipcode(drugMaster.getZipCode());
        drugRequest.setGood_rx_id(blinkId);

        drugRequestRepository.save(drugRequest);
    }
    public void drugMasterToGoodRxRequest(DrugMaster drugMaster, String goodRxId){
        DrugRequest drugRequest = new DrugRequest();
        drugRequest.setProgramId(6);
        if(drugMaster.getZipCode().equals("90036")){
            drugRequest.setLongitude("-118.3520389");
            drugRequest.setLatitude("34.0664817");
        }
        else if(drugMaster.getZipCode().equals("30606")){
            drugRequest.setLongitude("-83.4323375");
            drugRequest.setLatitude("33.9448436");
        }
        else if(drugMaster.getZipCode().equals("60639")){
            drugRequest.setLongitude("-87.7517295");
            drugRequest.setLatitude("41.9225138");
        }
        else if(drugMaster.getZipCode().equals("10023")){
            drugRequest.setLongitude("-73.9800645");
            drugRequest.setLatitude("40.7769059");
        }
        else if(drugMaster.getZipCode().equals("75034")){
            drugRequest.setLongitude("-96.8565427");
            drugRequest.setLatitude("33.1376528");
        }
        drugRequest.setDrugName(drugMaster.getName());
        drugRequest.setDrugId(drugMaster.getId()+"");

        if(drugMaster.getDrugType().equals("BRAND_WITH_GENERIC")){
            drugRequest.setBrandIndicator("B");
        }else if(drugMaster.getDrugType().equals("GENERIC")){
            drugRequest.setBrandIndicator("G");
        }else{
            drugRequest.setBrandIndicator(drugMaster.getDrugType());
        }

        drugRequest.setQuantity(drugMaster.getQuantity()+"");
        drugRequest.setZipcode(drugMaster.getZipCode());
        drugRequest.setGood_rx_id(goodRxId);

        drugRequestRepository.save(drugRequest);
    }

}