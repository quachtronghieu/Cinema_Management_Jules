package vn.edu.fpt.cinemamanagement.services;

import vn.edu.fpt.cinemamanagement.entities.Template;
import vn.edu.fpt.cinemamanagement.repositories.TemplateRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {
    private TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional
    public String getTemplateNameByID(String templateID) {
        return templateRepository.findById(templateID)
                .map(Template::getName)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Template có ID: " + templateID));
    }

    public Template getTemplateByID(String templateID) {
        return templateRepository.findById(templateID).orElse(null);
    }

}
