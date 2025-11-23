package com.g3.parking.service;

import com.g3.parking.request.InvoiceRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class InvoiceService {

	/**
	 * Genera un PDF de factura simple y lo devuelve como array de bytes.
	 * El DTO puede adaptarse según tus necesidades (añadir líneas, impuestos, etc.).
	 */
	public byte[] generateInvoicePdf(InvoiceRequest req) throws IOException {
		Objects.requireNonNull(req, "InvoiceRequest required");

		try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);

			try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
				float margin = 50;
				float yStart = page.getMediaBox().getHeight() - margin;
				float leading = 14f;
				float x = margin;
				float y = yStart;

				cs.beginText();
				cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
				cs.newLineAtOffset(x, y);
				cs.showText(req.getParkingName() != null ? req.getParkingName() : "Parking");
				cs.endText();

				y -= leading * 2;

				cs.beginText();
				cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
				cs.newLineAtOffset(x, y);
				cs.showText("Factura: " + (req.getInvoiceId() != null ? req.getInvoiceId() : "-"));
				cs.endText();

				y -= leading;

				cs.beginText();
				cs.setFont(PDType1Font.HELVETICA, 12);
				cs.newLineAtOffset(x, y);
				String dateText = req.getDate() != null ? req.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "-";
				cs.showText("Fecha: " + dateText);
				cs.endText();

				y -= leading * 2;

				cs.beginText();
				cs.setFont(PDType1Font.HELVETICA, 12);
				cs.newLineAtOffset(x, y);
				cs.showText("Pagador: " + (req.getPayerName() != null ? req.getPayerName() : "-"));
				cs.endText();

				y -= leading;

				cs.beginText();
				cs.setFont(PDType1Font.HELVETICA, 12);
				cs.newLineAtOffset(x, y);
				cs.showText("Descripción: " + (req.getDescription() != null ? req.getDescription() : "-"));
				cs.endText();

				y -= leading * 2;

				// Información del parqueadero
				if (req.getParkingAddress() != null) {
					cs.beginText();
					cs.setFont(PDType1Font.HELVETICA, 10);
					cs.newLineAtOffset(x, y);
					cs.showText("Dirección: " + req.getParkingAddress());
					cs.endText();
					y -= leading;
				}

				if (req.getParkingOrganization() != null) {
					cs.beginText();
					cs.setFont(PDType1Font.HELVETICA, 10);
					cs.newLineAtOffset(x, y);
					cs.showText("Organización: " + req.getParkingOrganization());
					cs.endText();
					y -= leading * 2;
				}

				// Información del vehículo
				if (req.getVehicleLicensePlate() != null) {
					cs.beginText();
					cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
					cs.newLineAtOffset(x, y);
					cs.showText("Información del Vehículo");
					cs.endText();
					y -= leading;

					cs.beginText();
					cs.setFont(PDType1Font.HELVETICA, 10);
					cs.newLineAtOffset(x, y);
					cs.showText("Placa: " + req.getVehicleLicensePlate());
					cs.endText();
					y -= leading;

					if (req.getVehicleCategory() != null) {
						cs.beginText();
						cs.setFont(PDType1Font.HELVETICA, 10);
						cs.newLineAtOffset(x, y);
						cs.showText("Categoría: " + req.getVehicleCategory());
						cs.endText();
						y -= leading;
					}

					if (req.getVehicleColor() != null) {
						cs.beginText();
						cs.setFont(PDType1Font.HELVETICA, 10);
						cs.newLineAtOffset(x, y);
						cs.showText("Color: " + req.getVehicleColor());
						cs.endText();
						y -= leading * 2;
					}
				}

				// Horarios
				if (req.getEntryTime() != null) {
					cs.beginText();
					cs.setFont(PDType1Font.HELVETICA, 10);
					cs.newLineAtOffset(x, y);
					String entryText = req.getEntryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
					cs.showText("Entrada: " + entryText);
					cs.endText();
					y -= leading;
				}

				if (req.getExitTime() != null) {
					cs.beginText();
					cs.setFont(PDType1Font.HELVETICA, 10);
					cs.newLineAtOffset(x, y);
					String exitText = req.getExitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
					cs.showText("Salida: " + exitText);
					cs.endText();
					y -= leading * 2;
				}

				// Descuento si aplica
				if (req.getDiscount() != null && req.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
					cs.beginText();
					cs.setFont(PDType1Font.HELVETICA, 10);
					cs.newLineAtOffset(x, y);
					cs.showText("Descuento: " + req.getDiscount().toPlainString() + " COP");
					cs.endText();
					y -= leading;
				}

				cs.beginText();
				cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
				cs.newLineAtOffset(x, y);
				String amountTxt = req.getAmount() != null ? req.getAmount().toPlainString() : "0.00";
				cs.showText("Total: " + amountTxt + " COP");
				cs.endText();

			}

			doc.save(out);
			return out.toByteArray();
		}
	}

}
