package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price,Integer> {


    List<Price> findByDrugDetailsId(int drugDetailsId);


    @Query(value= "SELECT * FROM price WHERE drug_details_id = ?1 AND program_id= ?2 ORDER BY id DESC"+
            " LIMIT 2",nativeQuery = true)
    List<Price> findLastPrice(int id,int programId);

    @Query(value = "SELECT price.* FROM report_drugs right outer join price on report_drugs.price_id = price.id " +
            " WHERE report_id = ?2 AND drug_details_id = ?1 ORDER BY price.program_id, price.rank", nativeQuery = true)
    List<Price> findRecentPricesByDrugId(int id, int report_id);

    @Query(value = "SELECT price.* from price" +
            " right outer join report_drugs on report_drugs.price_id = price.id" +
            " where report_drugs.report_id = ?3 and price.drug_details_id = ?1 and price.rank = ?2 ORDER BY price.program_id ", nativeQuery = true)

    List<Price> findByDrugDetailsIdAndRankAndReportId(int id, int i, int report_id);
    @Query(value = "SELECT price.* from price" +
            " right outer join report_drugs on report_drugs.price_id = price.id" +
            " where report_drugs.report_id = ?1", nativeQuery = true)
    List<Price> findAllPricesInReport(int reportId);

    @Query(value = "SELECT price.* from price" +
            " right outer join report_drugs on report_drugs.price_id = price.id" +
            " where report_drugs.report_id = ?1 and price.drug_details_id = ?2 and price.rank = ?3 and price.program_id = ?4 ORDER BY price.program_id LIMIT 1 ", nativeQuery = true)
    Price getOldPriceInReport(int reportId, int drugDetailsId, int rank, int programId);
}
