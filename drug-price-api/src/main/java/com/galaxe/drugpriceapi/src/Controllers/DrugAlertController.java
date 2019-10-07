package com.galaxe.drugpriceapi.src.Controllers;

import com.galaxe.drugpriceapi.src.Repositories.AlertRepository;
import com.galaxe.drugpriceapi.src.Repositories.AlertTypeRepository;
import com.galaxe.drugpriceapi.src.TableModels.Alert;
import com.galaxe.drugpriceapi.src.TableModels.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        alert.setTime(new Date());
        return this.alertRepository.save(alert);
    }
    @PostMapping("/create/alert/type")
    public AlertType createAlertType(@RequestBody  AlertType alertType){
        alertType.setActive(true);
        return this.alertTypeRepository.save(alertType);
    }


}