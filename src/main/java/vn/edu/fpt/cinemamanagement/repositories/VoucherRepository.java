package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cinemamanagement.entities.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {

    @Query(value = "SELECT TOP 1 voucher_id FROM Voucher WHERE voucher_id LIKE 'VC1%' ORDER BY voucher_id DESC", nativeQuery = true)
    public String getMaxAmountVoucherID();

    @Query(value = "SELECT TOP 1 voucher_id FROM Voucher WHERE voucher_id LIKE 'VC2%' ORDER BY voucher_id DESC", nativeQuery = true)
    public String getMaxPercentageVoucherID();

    @Query(value = "SELECT * FROM Voucher WHERE code = :code", nativeQuery = true)
    Voucher findByVoucherCode(@Param("code") String code);
}
