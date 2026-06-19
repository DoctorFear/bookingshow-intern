package com.bookingshow.util;

import com.bookingshow.entity.Booking;
import com.bookingshow.entity.BookingItem;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class PdfGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    public byte[] generateBookingConfirmation(Booking booking) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("BOOKING CONFIRMATION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Booking Info
            document.add(new Paragraph("Booking Reference: " + booking.getReference()));
            document.add(new Paragraph("Status: " + booking.getStatus()));

            String createdDate = FORMATTER.format(booking.getCreatedAt());
            document.add(new Paragraph("Created: " + createdDate));

            document.add(new Paragraph("Customer: " + booking.getCustomer().getName()
                    + " (" + booking.getCustomer().getEmail() + ")"));
            document.add(new Paragraph(" "));

            // Tickets Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 2f, 1f, 2f, 2f});

            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            table.addCell(new PdfPCell(new Phrase("Event", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Ticket Type", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Qty", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Unit Price", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Subtotal", headerFont)));

            for (BookingItem item : booking.getBookingItems()) {
                table.addCell(item.getTicketType().getEvent().getTitle());
                table.addCell(item.getTicketType().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(item.getUnitPrice() + " VNĐ");
                table.addCell(item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())) + " VNĐ");
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Total
            Font totalFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Paragraph totalPara = new Paragraph("Total Amount: " + booking.getTotalAmount() + " VNĐ", totalFont);
            totalPara.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalPara);

            document.add(new Paragraph("Thank you for booking with us!",
                    new Font(Font.HELVETICA, 12, Font.ITALIC)));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
}