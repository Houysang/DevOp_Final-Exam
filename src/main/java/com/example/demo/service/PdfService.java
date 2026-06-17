package com.example.demo.service;

import com.example.demo.model.Profile;
import com.example.demo.model.Template;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Image;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    private static final float CARD_WIDTH = 340f;
    private static final float CARD_HEIGHT = 220f;

    /**
     * Generate a single ID card PDF for a profile
     */
    public byte[] generateIdCardPdf(Profile profile) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(new Rectangle(CARD_WIDTH, CARD_HEIGHT), 10, 10, 10, 10);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Draw card background
            PdfContentByte canvas = writer.getDirectContent();
            drawCardBackground(canvas);

            // Add photo if available
            if (profile.getPhoto() != null && profile.getPhoto().length > 0) {
                addPhotoToPdf(document, profile.getPhoto(), 15, 40, 80, 100);
            }

            // Add text details
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);

            // Institution header
            Paragraph header = new Paragraph("ID CARD", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            document.add(new Paragraph(" ")); // spacer

            // Profile type
            Paragraph typeLabel = new Paragraph("TYPE: " + profile.getProfileType().toString(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.DARK_GRAY));
            typeLabel.setAlignment(Element.ALIGN_CENTER);
            document.add(typeLabel);

            document.add(new Paragraph(" ")); // spacer

            // Details
            float textStartX = 105f;
            addCardDetail(document, "Name:", profile.getFullName(), 10, labelFont, valueFont);
            document.add(new Chunk(" "));
            addCardDetail(document, "ID:", profile.getUniqueId(), 10, labelFont, valueFont);
            document.add(new Chunk(" "));
            addCardDetail(document, "Dept:", profile.getDepartment() != null ? profile.getDepartment() : "N/A", 10, labelFont, valueFont);
            document.add(new Chunk(" "));
            addCardDetail(document, "Position:", profile.getPosition() != null ? profile.getPosition() : "N/A", 10, labelFont, valueFont);

            document.close();
        } catch (DocumentException | IOException e) {
            throw e;
        }

        return baos.toByteArray();
    }

    /**
     * Generate a single ID card PDF with QR code
     */
    public byte[] generateIdCardPdfWithQR(Profile profile, byte[] qrCodeImage, byte[] barcodeImage)
            throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(new Rectangle(CARD_WIDTH + 100, CARD_HEIGHT + 50), 10, 10, 10, 10);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            PdfContentByte canvas = writer.getDirectContent();
            drawCardBackground(canvas);

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("OFFICIAL ID CARD", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // Profile type
            Paragraph typeLabel = new Paragraph(profile.getProfileType().toString(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(0, 102, 204)));
            typeLabel.setAlignment(Element.ALIGN_CENTER);
            document.add(typeLabel);

            document.add(new Paragraph(" "));

            // Photo
            if (profile.getPhoto() != null && profile.getPhoto().length > 0) {
                addPhotoToPdf(document, profile.getPhoto(), 15, 95, 90, 110);
            }

            // Details
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

            addCardDetail(document, "Full Name:", profile.getFullName(), 115, labelFont, valueFont);
            addCardDetail(document, "Unique ID:", profile.getUniqueId(), 115, labelFont, valueFont);
            addCardDetail(document, "Department:", profile.getDepartment() != null ? profile.getDepartment() : "N/A", 115, labelFont, valueFont);
            addCardDetail(document, "Position:", profile.getPosition() != null ? profile.getPosition() : "N/A", 115, labelFont, valueFont);

            document.add(new Paragraph(" "));

            // QR Code
            if (qrCodeImage != null && qrCodeImage.length > 0) {
                Image qrImg = Image.getInstance(qrCodeImage);
                qrImg.scaleToFit(80, 80);
                qrImg.setAbsolutePosition(15, 15);
                document.add(qrImg);
            }

            // Barcode
            if (barcodeImage != null && barcodeImage.length > 0) {
                Image barcodeImg = Image.getInstance(barcodeImage);
                barcodeImg.scaleToFit(150, 40);
                barcodeImg.setAbsolutePosition(120, 30);
                document.add(barcodeImg);
            }

            document.close();
        } catch (DocumentException | IOException e) {
            throw e;
        }

        return baos.toByteArray();
    }

    /**
     * Generate batch PDF for multiple profiles
     */
    public byte[] generateBatchIdCardsPdf(List<Profile> profiles, byte[][] qrCodes, byte[][] barcodes)
            throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Paragraph mainTitle = new Paragraph("BATCH ID CARDS", titleFont);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(mainTitle);
            document.add(new Paragraph(" "));

            for (int i = 0; i < profiles.size(); i++) {
                Profile profile = profiles.get(i);

                // Add a new page for each card (except first)
                if (i > 0) {
                    document.newPage();
                }

                drawCardBackground(writer.getDirectContent());

                // Title
                Paragraph title = new Paragraph("ID CARD - " + profile.getProfileType().toString(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY));
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph(" "));

                // Photo
                if (profile.getPhoto() != null && profile.getPhoto().length > 0) {
                    addPhotoToPdf(document, profile.getPhoto(), 20, 120, 90, 110);
                }

                // Details
                Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GRAY);
                Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

                addCardDetail(document, "Name:", profile.getFullName(), 130, labelFont, valueFont);
                addCardDetail(document, "ID:", profile.getUniqueId(), 130, labelFont, valueFont);
                addCardDetail(document, "Dept:", profile.getDepartment(), 130, labelFont, valueFont);
                addCardDetail(document, "Position:", profile.getPosition(), 130, labelFont, valueFont);

                // QR Code
                if (qrCodes != null && i < qrCodes.length && qrCodes[i] != null) {
                    Image qrImg = Image.getInstance(qrCodes[i]);
                    qrImg.scaleToFit(70, 70);
                    qrImg.setAbsolutePosition(20, 20);
                    document.add(qrImg);
                }

                // Barcode
                if (barcodes != null && i < barcodes.length && barcodes[i] != null) {
                    Image barcodeImg = Image.getInstance(barcodes[i]);
                    barcodeImg.scaleToFit(130, 35);
                    barcodeImg.setAbsolutePosition(120, 35);
                    document.add(barcodeImg);
                }
            }

            document.close();
        } catch (DocumentException | IOException e) {
            throw e;
        }

        return baos.toByteArray();
    }

    private void drawCardBackground(PdfContentByte canvas) {
        // Simple white background with border
        canvas.setColorStroke(BaseColor.LIGHT_GRAY);
        canvas.setColorFill(BaseColor.WHITE);
        canvas.roundRectangle(5, 5, CARD_WIDTH - 10, CARD_HEIGHT - 10, 10);
        canvas.fillStroke();
    }

    private void addPhotoToPdf(Document document, byte[] photoBytes, float x, float y, float width, float height)
            throws DocumentException, IOException {
        try {
            Image img = Image.getInstance(photoBytes);
            img.scaleToFit(width, height);
            img.setAbsolutePosition(x, y);
            document.add(img);
        } catch (Exception e) {
            // If photo can't be added, skip it
        }
    }

    private void addCardDetail(Document document, String label, String value, float textX,
                                Font labelFont, Font valueFont) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setWidths(new float[]{30, 70});

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(2);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(2);
        table.addCell(valueCell);

        document.add(table);
    }

    private void addCardDetail(Document document, String label, String value, int indent,
                                Font labelFont, Font valueFont) throws DocumentException {
        Paragraph p = new Paragraph();
        p.setIndentationLeft(indent);
        Chunk labelChunk = new Chunk(label + " ", labelFont);
        Chunk valueChunk = new Chunk(value != null ? value : "N/A", valueFont);
        p.add(labelChunk);
        p.add(valueChunk);
        document.add(p);
    }
}