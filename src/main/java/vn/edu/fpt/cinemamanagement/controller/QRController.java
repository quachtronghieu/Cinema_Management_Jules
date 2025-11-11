package vn.edu.fpt.cinemamanagement.controller;



import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.services.QRCodeGenerationService;

@RestController
@RequestMapping("/qrpayment")
public class QRController {

    @GetMapping(value = "/qrpayment", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] generateQRCode(@RequestParam String idpay) throws Exception {
        return QRCodeGenerationService.generateQRCode(idpay, 300, 300);
    }
}
