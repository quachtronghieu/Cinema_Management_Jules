package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cinemamanagement.entities.Concession;
import vn.edu.fpt.cinemamanagement.services.ConcessionService;

import java.util.Map;

@Controller
@RequestMapping("/concessions")
public class ConcessionController {

    private final ConcessionService service;

    public ConcessionController(ConcessionService service) {
        this.service = service;
    }

    // LIST + PAGINATION (y như voucher)
    @GetMapping("")
    public String listConcessions(@RequestParam(name = "page", defaultValue = "1") int page,
                                  Model model) {

        int pageSize = 5;
        Page<Concession> p = service.findPage(page, pageSize);

        int totalPages = Math.max(p.getTotalPages(), 1);
        int currentPage = Math.min(Math.max(page, 1), totalPages);

        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);
        if (endPage - startPage < 4) startPage = Math.max(1, endPage - 4);

        model.addAttribute("concessionsList", p.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "concession/concession_list";
    }

    // CREATE (form)
    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("concession")) {
            model.addAttribute("concession", new Concession());
        }
        model.addAttribute("imageFiles", service.listImageFiles());
        return "concession/concession_create";
    }

    // CREATE (submit) — toàn bộ validate ở Service
    @PostMapping("/create")
    public String create(@ModelAttribute Concession concession,
                         @RequestParam(name = "type") String type,
                         @RequestParam(name = "imageFile", required = false) String imageFile,
                         Model model,
                         RedirectAttributes ra) {

        Map<String, String> errors = service.createWithPrefix(type, concession, imageFile);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("imageFiles", service.listImageFiles());
            return "concession/concession_create";
        }

        ra.addFlashAttribute("message", "Created successfully");
        return "redirect:/concessions";
    }

    // DETAIL
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") String id, Model model) {
        model.addAttribute("concession", service.findById(id));
        return "concession/concession_detail";
    }

    // EDIT (form)
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id")  String id, Model model) {
        model.addAttribute("concession", service.findById(id));
        model.addAttribute("imageFiles", service.listImageFiles());
        return "concession/concession_update";
    }

    // EDIT (submit) — toàn bộ validate ở Service
    @PostMapping("/edit/{id}")
    public String update(@PathVariable("id") String id,
                         @ModelAttribute Concession concession,
                         @RequestParam(name = "imageFile", required = false) String imageFile,
                         Model model,
                         RedirectAttributes ra) {

        Map<String, String> errors = service.update(id, concession, imageFile);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("concession", concession);
            model.addAttribute("imageFiles", service.listImageFiles());
            return "concession/concession_update";
        }

        ra.addFlashAttribute("message", "Updated successfully");
        return "redirect:/concessions";
    }

    // DELETE (modal post giống voucher)
    @PostMapping("/delete")
    public String deleteConcession(@RequestParam("concessionId") String id,
                                   RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("msg", "Deleted " + id + " successfully.");
        return "redirect:/concessions";
    }
}
