package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.PriceLeadingDrug;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price,Integer> {

    Price findPriceById(Integer id);

    List<Price> findByDrugDetailsId(int drugDetailsId);


    @Query(value= "SELECT * FROM price WHERE drug_details_id = ?1 AND program_id= ?2 ORDER BY id DESC"+
            " LIMIT 2",nativeQuery = true)
    List<Price> findLastPrice(int id,int programId);

    @Query(value = "SELECT price.* FROM report_drugs right outer join price on report_drugs.price_id = price.id " +
            " WHERE report_id = ?2 AND drug_details_id = ?1 ORDER BY price.program_id, price.rank", nativeQuery = true)
    List<Price> findRecentPricesByDrugId(int id, int report_id);

    @Query(value = "SELECT price.* from price" +
            " right outer join report_drugs on report_drugs.price_id = price.id" +
            " where report_drugs.report_id = ?2 and price.drug_details_id = ?1 ORDER BY price.program_id ", nativeQuery = true)
    List<Price> findByDrugDetailsIdAndRankAndReportId(int id, int report_id);

    @Query(value = "select new com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.PriceLeadingDrug(p.id, d.name, d.dosageStrength, d.quantity, d.ndc, p.recommendedPrice, p.price, p.averagePrice, p.drugDetailsId,p.pharmacy, p.programId," +
            "p.rank, d.zipCode,r.reportId, r.priceId,d.id) from Price p left join Report_Drugs r on p.id = r.priceId left join DrugMaster d on d.id = p.drugDetailsId where r.reportId = ?1 and " +
            "p.programId = 0 and p.price = p.recommendedPrice order by d.name asc")
    List<PriceLeadingDrug> findInsideRxLeadingPrices(int report_id);

    @Query(value = "select * from price where id in ?1 order by program_id", nativeQuery = true)
    List<Price> findAllFromList(List<Integer> priceIds);
}
