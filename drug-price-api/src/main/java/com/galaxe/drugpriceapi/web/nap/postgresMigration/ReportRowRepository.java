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

    @Query(value= " SELECT " +
            "            t.name, ROW_NUMBER() OVER (ORDER BY t.name) AS id, t.drug_id,\n" +
            "            t.dosage_strength, t.quantity,  t.ndc, t.gsn,  t.recommended_price, t.zip_code, \n" +
            "            max(case when t.program_id = 0  then t.price end) AS insiderx_price, \n" +
            " max(case when t.program_id = 0  then t.pharmacy end) AS insiderx_pharmacy, \n" +
            "            max(case when t.program_id = 1  then t.price end) AS pharm_price, \n" +
            " max(case when t.program_id = 1  then t.pharmacy end) AS pharm_pharmacy, \n" +
            "            max(case when t.program_id = 2  then t.price end) AS wellrx_price, \n" +
            " max(case when t.program_id = 2  then t.pharmacy end) AS wellrx_pharmacy, \n" +
            "            max(case when t.program_id = 3  then t.price end) AS medimpact_price, \n" +
            " max(case when t.program_id = 3  then t.pharmacy end) AS medimpact_pharmacy, \n" +
            "              max(case when t.program_id = 4  then t.price end) AS singlecare_price, \n" +
            " max(case when t.program_id = 4  then t.pharmacy end) AS singlecare_pharmacy, \n" +
            "              max(case when t.program_id = 6  then t.price end) AS goodrx_price,\n" +
            " max(case when t.program_id = 6  then t.pharmacy end) AS goodrx_pharmacy,\n" +
            "              max(case when t.program_id = 5  then t.price end) AS blink_price ,\n" +
            " max(case when t.program_id = 5  then t.pharmacy end) AS blink_pharmacy\n" +
            "              FROM \n" +
            "            ( SELECT  drug_master.name, drug_master.id as drug_id, drug_master.ndc, drug_master.gsn,drug_master.zip_code,  price.price, price.program_id, \n" +
            "             drug_master.quantity, drug_master.dosage_strength, price.recommended_price , price.pharmacy  \n" +
            "             from report_drugs full outer join price on  price.id = report_drugs.price_id\n" +
            "             full outer join drug_master on price.drug_details_id = drug_master.id \n" +
            " where report_drugs.report_id = ?1) t\n" +

            "              GROUP BY t.name , t.dosage_strength,t.drug_id, t.quantity, t.ndc, t.gsn, t.recommended_price, t.zip_code\n" +
            "              ORDER BY t.name, t.dosage_strength ;" , nativeQuery = true)
        List<ReportRow> exportReport(Integer reportId);
}
