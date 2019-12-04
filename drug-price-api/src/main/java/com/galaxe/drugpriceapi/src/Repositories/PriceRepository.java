package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price,Integer> {

    @Query(value =
            "SELECT price.* FROM price WHERE price.id IN" +
                "(SELECT p1.price_id FROM" +
                    "(SELECT report_drugs.price_id, report_drugs.report_id FROM report_drugs WHERE report_id IN " +
                        "(SELECT report_table.id FROM report_table ORDER BY timestamp DESC LIMIT 1)" +
                    ")" +
                "AS p1)" +
            "AND price.drug_details_id = ?1 AND price.program_id = ?2 ORDER BY rank", nativeQuery = true)
    List<Price> findLatestPriceForDrug(Integer drugDetailsId, Integer program_id);

    List<Price> findByDrugDetailsId(int drugDetailsId);


    @Query(value= "SELECT price.* FROM price WHERE drug_details_id = ?1 AND program_id= ?2 ORDER BY id DESC"+
            " LIMIT 2",nativeQuery = true)
    List<Price> findLastPrice(int id,int programId);

    @Query(value = "SELECT price.* FROM report_drugs right outer join price on report_drugs.price_id = price.id " +
            " WHERE rank = 0 and  report_id = ?2 AND drug_details_id = ?1 ORDER BY price.program_id, price.rank", nativeQuery = true)
    List<Price> findRecentPricesByDrugId(int id, int report_id);

    @Query(value = "SELECT price.* from price" +
            " right outer join report_drugs on report_drugs.price_id = price.id" +
            " where report_drugs.report_id = ?3 and price.drug_details_id = ?1 and price.rank = ?2 ORDER BY price.program_id ", nativeQuery = true)
    List<Price> findByDrugDetailsIdAndRankAndReportId(int id, int i, int report_id);

    @Query(value = "SELECT price.* from price " +
            "right outer join report_drugs on report_drugs.price_id = price.id " +
            "where price.drug_details_id = ?1 and price.rank = ?2 and report_drugs.report_id = ?3 and price.program_id = ?4 ", nativeQuery = true)
    List<Price> findByDrugDetailsIdAndRankAndReportIdAndProgramId(int drugDetailsId, int rank, int reportId, int programId);

    @Query(value = "SELECT price.* from price" +
            " right outer join report_drugs on report_drugs.price_id = price.id" +
            " where report_drugs.report_id = ?1 and price.drug_details_id in (\n" +
            "            select drug_id from report_dm\n" +
            "    ) and price.rank = 0 ORDER BY price.program_id ", nativeQuery = true)
    List<Price> findDashboardDrugPrices(int reportId);

}
