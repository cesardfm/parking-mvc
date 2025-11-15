package com.g3.parking.controller.api;

import com.g3.parking.request.InvoiceRequest;
import com.g3.parking.service.SendEmail;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/publico/invoices")
public class SendEmailController {

	private final SendEmail sendEmail;

	public SendEmailController(SendEmail sendEmail) {
		this.sendEmail = sendEmail;
	}

	/**
	 * Envía por email la factura representada por InvoiceRequest.
	 * Parámetros:
	 * - to: email destino (query param)
	 * - body: InvoiceRequest JSON
	 */
	@PostMapping("/send")
	public ResponseEntity<Map<String, Object>> sendInvoiceEmail(@RequestParam("to") String to,
																@RequestBody InvoiceRequest req) {
		Map<String, Object> resp = new HashMap<>();

		if (to == null || to.isBlank()) {
			resp.put("success", false);
			resp.put("message", "Missing 'to' parameter");
			return ResponseEntity.badRequest().body(resp);
		}

		try {
			sendEmail.sendInvoiceEmail(to, req);
			resp.put("success", true);
			resp.put("message", "Email enviado");
			return ResponseEntity.ok(resp);
		} catch (MessagingException | IOException e) {
			resp.put("success", false);
			resp.put("message", "Error enviando email: " + e.getMessage());
			return ResponseEntity.status(500).body(resp);
		}
	}

}
