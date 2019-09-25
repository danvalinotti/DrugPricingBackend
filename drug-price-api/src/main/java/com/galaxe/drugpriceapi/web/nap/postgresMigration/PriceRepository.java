package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugMaster;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price,Integer> {

    @Query(value= "SELECT * FROM price FULL OUTER JOIN drug_master \n" +
            "ON price.drugmaster_id = drug_master.id \n" +
            "FULL OUTER JOIN program_master \n" +
            "ON price.programmaster_id = program_master.id " +
            "WHERE ndc = ?2 AND drug_master.quantity = ?1 \n" +
            "AND price.id NOTNULL AND program_master.name = ?3",nativeQuery = true)
    List<Price> findAllByFields(double quantity, String ndc, String programName);

    List<Price> findByDrugDetailsId(int drugDetailsId);

    @Query(value= "SELECT * FROM price WHERE drug_details_id = ?1 ORDER BY id DESC"+
            " LIMIT 6",nativeQuery = true)
    List<Price> findByRecentDrugDetails(int id);

    @Query(value= "SELECT * FROM price WHERE drug_details_id = ?1 AND program_id= ?2 ORDER BY id DESC"+
            " LIMIT 2",nativeQuery = true)
    List<Price> findLastPrice(int id,int programId);

    @Query(value = "SELECT price.* " +
            "FROM report_drugs right outer join price on report_drugs.price_id = price.id " +
            " WHERE report_id = ?2 AND drug_details_id = ?1 ORDER BY price.program_id", nativeQuery = true)
    List<Price> findRecentPricesByDrugId(int id, int report_id);

//select MIN(id) drug_details_id, program_id, rank from public_price group by  drug_details_id ,program_id,rank order by MIN(id) asc

//    List<Price> findAllByDrugmaster(DrugMaster drug);

//    List<Price> findByDrugDetailsDrugMasterId(int id);
}
