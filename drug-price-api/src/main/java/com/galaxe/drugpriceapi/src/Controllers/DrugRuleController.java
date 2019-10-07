package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Repositories.AlertRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRuleRepository;
import com.galaxe.drugpriceapi.src.Repositories.PriceRepository;
import com.galaxe.drugpriceapi.src.Repositories.ReportDrugsRepository;
import com.galaxe.drugpriceapi.src.TableModels.Alert;
import com.galaxe.drugpriceapi.src.TableModels.DrugRule;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import com.galaxe.drugpriceapi.src.TableModels.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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



   @PostMapping("/create/drug/rule")
    public DrugRule createRule (@RequestBody DrugRule drugRule){

       return this.drugRuleRepository.save(drugRule);
   }

    public void checkRules(Report report) {
        List<Price> prices = priceRepository.findAllById(reportDrugsRepository.findPriceIdByReportId(report.getId()));
        List<DrugRule> drugRules = drugRuleRepository.findAll();

        for (Price price :prices) {
            for (DrugRule rule :drugRules) {
                if(rule.getDrugId() == 0 || rule.getDrugId() == price.getDrugDetailsId()){
                    double drugPerc =price.getDifference()/(price.getPrice()+price.getDifference())*100;
                    if(rule.getPercentChange()<= drugPerc){
                        Alert alert  = new Alert();
                        alert.setName("Price Change");
                        alert.setDetailedMessage("There was a "+drugPerc+"% change");
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