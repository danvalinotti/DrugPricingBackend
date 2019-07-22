package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Alert;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Profile;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.StringSender;
import com.galaxe.drugpriceapi.web.nap.wellRx.WellRx;
import com.galaxe.drugpriceapi.web.nap.wellRx.WellRxGSNSearch;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Date;
import java.util.List;


@CrossOrigin
@RestController
public class DrugAlertController {

    @Autowired
    AlertRepository alertRepository;

    @GetMapping("/get/alerts/all")
    public List<Alert> getAll(){
       return this.alertRepository.findAll();
    }

    @PostMapping("/send/alert")
    public Alert sendAlert(@RequestBody  Alert alert){
        //SEND ALERT CODE

        alert.setTime(new Date());
        return this.alertRepository.save(alert);
    }

    @PostMapping("/post")
    public WellRxGSNSearch testPost(@RequestBody WellRxGSNSearch w){
//        WellRxGSNSearch wellRxGSNSearch = new WellRxGSNSearch();
//        wellRxGSNSearch.setGSN("006582");
//        wellRxGSNSearch.setLat("40.5827122");
//        wellRxGSNSearch.setLng("-74.2707509");
//        wellRxGSNSearch.setNumdrugs("1");
//        wellRxGSNSearch.setQuantity("1");
//        wellRxGSNSearch.setBgIndicator("B");
//        wellRxGSNSearch.setbReference("HUMATROPE");
//        wellRxGSNSearch.setNcpdps("null");
        return w;
    }

}