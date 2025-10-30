package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.entities.Staff;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByPassword(String password);
    @Query(value = "SELECT TOP 1 user_id FROM Customer ORDER BY user_id DESC", nativeQuery = true)
    String findLastCustomerId();
    //login
    Optional<Customer>  findByUsername(String username);

    Customer findByEmail(String email);

    List<Customer> findByVerifyAndResetRequestedAtBefore(String verify, LocalDateTime time);
}
