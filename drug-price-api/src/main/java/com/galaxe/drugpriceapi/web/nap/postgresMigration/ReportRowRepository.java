package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Price;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportRowRepository extends JpaRepository<ReportRow,Integer> {

    @Query(value= "SELECT " +
            " t.name, ROW_NUMBER() OVER (ORDER BY t.name) AS id, " +
            " t.dosage_strength, t.quantity,  t.ndc, t.gsn,  t.recommended_price, " +
            " max(case when t.program_id = 0  then t.price end) AS insiderx_price, " +
            " max(case when t.program_id = 1  then t.price end) AS pharm_price, " +
            " max(case when t.program_id = 2  then t.price end) AS wellrx_price, " +
            " max(case when t.program_id = 3  then t.price end) AS medimpact_price, " +
            " max(case when t.program_id = 4  then t.price end) AS singlecare_price, " +
            " max(case when t.program_id = 5  then t.price end) AS blink_price " +
            " FROM " +
            " (SELECT  drug_master.name,  drug_master.ndc, drug_master.gsn,  price.price, price.program_id, " +
            " drug_master.quantity, drug_master.dosage_strength, price.recommended_price  from report_drugs " +
            " Full outer join price ON price.id = report_drugs.price_id " +
            " full outer join drug_master on price.drug_details_id = drug_master.id " +
            " WHERE report_id = ?1) t " +
            " GROUP BY t.name , t.dosage_strength, t.quantity, t.ndc, t.gsn, t.recommended_price" +
            " ORDER BY t.name ;" , nativeQuery = true)
        List<ReportRow> exportReport(Integer reportId);
}
