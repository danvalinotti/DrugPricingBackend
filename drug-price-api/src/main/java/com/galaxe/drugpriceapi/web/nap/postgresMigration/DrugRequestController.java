package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Alert;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.AlertType;
import com.galaxe.drugpriceapi.web.nap.wellRx.WellRxGSNSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;


@CrossOrigin
@RestController
public class DrugRequestController {

    @Autowired
    DrugRequestRepository drugRequestRepository;


}