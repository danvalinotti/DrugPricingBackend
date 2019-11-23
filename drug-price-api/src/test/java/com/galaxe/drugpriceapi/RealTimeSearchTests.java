package com.galaxe.drugpriceapi;

import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.galaxe.drugpriceapi.src.Services.RealTimeSearchService.getCompetitorPrice;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RealTimeSearchTests {

    @Test
    public void insideRxTest() {
        DrugRequest drugRequest = new DrugRequest();
        drugRequest.setNdc("");
        drugRequest.setGsn("");
        drugRequest.setDrugName("Acyclovir");
        drugRequest.setQuantity("60");
        drugRequest.setZipcode("92648");
        drugRequest.setLongitude("-118.002");
        drugRequest.setLatitude("33.673");
        drugRequest.setDosageStrength("400 mg");

        getCompetitorPrice(drugRequest, 0);

    }
}
