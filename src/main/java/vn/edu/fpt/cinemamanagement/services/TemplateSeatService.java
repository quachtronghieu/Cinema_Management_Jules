package vn.edu.fpt.cinemamanagement.services;

import vn.edu.fpt.cinemamanagement.entities.TemplateSeat;
import vn.edu.fpt.cinemamanagement.repositories.TemplateSeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateSeatService {
    private TemplateSeatRepository templateSeatRepository;

    public TemplateSeatService(TemplateSeatRepository templateSeatRepository) {
        this.templateSeatRepository = templateSeatRepository;
    }

    @Transactional
    public int countTotalSeatsByTemplateID(String templateID) {
        long total = templateSeatRepository.countByTemplate_Id(templateID);
        return (int) total;
    }

    @Transactional
    public List<TemplateSeat> findAllSeatsByTemplateID(String templateID) {
        return templateSeatRepository.findByTemplate_Id(templateID);
    }
}

