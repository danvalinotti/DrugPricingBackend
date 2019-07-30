package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
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
public class DrugRuleController {


    @Autowired
    DrugRuleRepository drugRuleRepository;
    @Autowired
    PriceRepository priceRepository;
    @Autowired
    ReportDrugsRepository reportDrugsRepository;
    @Autowired
    AlertRepository alertRepository;

    @GetMapping("/drug/rule/get/all")
    public List<DrugRule> getAllDrugRule (){

        return this.drugRuleRepository.findAll();
    }

   @PostMapping("/create/drug/rule")
    public DrugRule createRule (@RequestBody DrugRule drugRule){

       return this.drugRuleRepository.save(drugRule);
   }

    public void checkRules(Report report) {
        List<Price> prices = priceRepository.findAllById(reportDrugsRepository.findPriceIdByReportId(report.getId()));
        List<DrugRule> drugRules = drugRuleRepository.findAll();

        for (Price price :prices) {
            for (DrugRule rule :drugRules) {
                if(rule.getDrugId() == price.getDrugDetailsId()){
                    if(rule.getPercentChange()<= price.getDifference()/price.getPrice()){
                        Alert alert  = new Alert();
                        alert.setName("Price Change");
                        alert.setDetailedMessage("There was a "+price.getDifference()/price.getPrice()+"% change");
                        alert.setTime(new Date());
                        alert.setStatus("New");
                        alert.setAlertTypeId(rule.getAlertTypeId()+"");
                        alertRepository.save(alert);
                    }
                }
            }

        }
    }
}