package com.g3.parking.controller.web;

import com.g3.parking.request.InvoiceRequest;
import com.g3.parking.service.InvoiceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable String id) throws IOException {
        // Aquí deberías cargar datos reales por id; ejemplo mínimo:
        InvoiceRequest req = new InvoiceRequest();
        req.setInvoiceId(id);
        req.setPayerName("John Doe");
        req.setParkingName("Central Parking");
        req.setDescription("Pago ejemplo");
        req.setAmount(new java.math.BigDecimal("12.50"));
        req.setDate(java.time.LocalDateTime.now());

        byte[] pdf = invoiceService.generateInvoicePdf(req);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + id + ".pdf\"")
            .body(pdf);
    }
}