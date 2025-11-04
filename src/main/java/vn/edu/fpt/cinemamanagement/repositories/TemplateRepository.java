package vn.edu.fpt.cinemamanagement.repositories;

import vn.edu.fpt.cinemamanagement.entities.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends JpaRepository<Template, String> {
    Template findByName(String name);


}
