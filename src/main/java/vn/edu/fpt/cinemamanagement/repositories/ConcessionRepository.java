package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.cinemamanagement.entities.Concession;

import java.util.Optional;

public interface ConcessionRepository extends JpaRepository<Concession, String> {
    // Dùng để filter + phân trang theo prefix ID (PC/DR)
    Page<Concession> findByConcessionIdStartingWith(String prefix, Pageable pageable);

    // Lấy bản ghi có concessionId lớn nhất trong cùng prefix (top 1 desc) để sinh ID tiếp theo
    Optional<Concession> findTopByConcessionIdStartingWithOrderByConcessionIdDesc(String prefix);

}
