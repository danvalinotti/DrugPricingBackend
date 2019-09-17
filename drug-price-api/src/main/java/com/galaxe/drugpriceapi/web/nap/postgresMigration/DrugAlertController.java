package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Alert;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.AlertType;
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

    @Autowired
    AlertTypeRepository alertTypeRepository;

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
    @PostMapping("/create/alert/type")
    public AlertType createAlertType(@RequestBody  AlertType alertType){
        alertType.setActive(true);
        return this.alertTypeRepository.save(alertType);
    }
    @GetMapping("/test/alert")
    public Alert sendTestAlert(){
        Alert a = new Alert();
        a.setAlertTypeId("183565");
        a.setStatus("Complete");
        a.setTime(new Date());
        a.setDetailedMessage("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt " +
                "ut labore et dolore magna aliqua. Nunc pulvinar sapien et ligula ullamcorper malesuada. ");
        a.setName("Test Alert");

        return alertRepository.save(a);
    }

    @PostMapping("/post")
    public WellRxGSNSearch testPost(@RequestBody WellRxGSNSearch w){

        return w;
    }

}