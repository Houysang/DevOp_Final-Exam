package com.example.demo.service;

import com.example.demo.model.Profile;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Image;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    private static final float CARD_WIDTH = 420f;
    private static final float CARD_HEIGHT = 280f;

    /**
     * Generate a simple ID card PDF (photo in center, details below)
     */
    public byte[] generateIdCardPdf(Profile profile) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(new Rectangle(CARD_WIDTH, CARD_HEIGHT), 15, 15, 15, 15);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            PdfContentByte canvas = writer.getDirectContentUnder();
            drawCardBackground(canvas, CARD_WIDTH, CARD_HEIGHT);

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(0x2c, 0x3e, 0x50));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new BaseColor(0x1a, 0x73, 0xe8));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);

            Paragraph header = new Paragraph("ID CARD", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));

            Paragraph typeLabel = new Paragraph(profile.getProfileType().toString(), subtitleFont);
            typeLabel.setAlignment(Element.ALIGN_CENTER);
            document.add(typeLabel);
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 8)));

            // Photo in CENTER
            if (profile.getPhoto() != null && profile.getPhoto().length > 0) {
                try {
                    Image img = Image.getInstance(profile.getPhoto());
                    img.scaleToFit(80, 100);
                    img.setAbsolutePosition(170, 55);
                    writer.getDirectContent().addImage(img, true);
                } catch (Exception e) {
                    // skip photo on error
                }
            }

            // Details below photo
            addCardDetail(document, "Name:", profile.getFullName(), 15, labelFont, valueFont);
            addCardDetail(document, "ID:", profile.getUniqueId(), 15, labelFont, valueFont);
            addCardDetail(document, "Department:", profile.getDepartment() != null ? profile.getDepartment() : "N/A", 15, labelFont, valueFont);
            addCardDetail(document, "Position:", profile.getPosition() != null ? profile.getPosition() : "N/A", 15, labelFont, valueFont);
            addCardDetail(document, "Email:", profile.getEmail() != null ? profile.getEmail() : "N/A", 15, labelFont, valueFont);

            document.close();
        } catch (DocumentException e) {
            throw e;
        }

        return baos.toByteArray();
    }

    /**
     * Generate PDF with QR code (photo center, QR right, barcode bottom)
     */
    public byte[] generateIdCardPdfWithQR(Profile profile, byte[] qrCodeImage, byte[] barcodeImage)
            throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(new Rectangle(500, 320), 10, 10, 10, 10);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            float docWidth = 500f;
            float docHeight = 320f;

            PdfContentByte bgCanvas = writer.getDirectContentUnder();
            drawCardBackground(bgCanvas, docWidth, docHeight);

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(0x2c, 0x3e, 0x50));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new BaseColor(0x1a, 0x73, 0xe8));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            Paragraph spacer = new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 6));

            Paragraph title = new Paragraph("OFFICIAL ID CARD", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(spacer);

            Paragraph typeLabel = new Paragraph(profile.getProfileType().toString(), subtitleFont);
            typeLabel.setAlignment(Element.ALIGN_CENTER);
            document.add(typeLabel);
            document.add(spacer);

            // Photo in CENTER
            if (profile.getPhoto() != null && profile.getPhoto().length > 0) {
                try {
                    Image img = Image.getInstance(profile.getPhoto());
                    img.scaleToFit(90, 110);
                    img.setAbsolutePosition(205, 60);
                    writer.getDirectContent().addImage(img, true);
                } catch (Exception e) {
                    // skip photo on error
                }
            }

            // Details below photo
            addCardDetail(document, "Full Name:", profile.getFullName(), 15, labelFont, valueFont);
            addCardDetail(document, "Unique ID:", profile.getUniqueId(), 15, labelFont, valueFont);
            addCardDetail(document, "Department:", profile.getDepartment() != null ? profile.getDepartment() : "N/A", 15, labelFont, valueFont);
            addCardDetail(document, "Position:", profile.getPosition() != null ? profile.getPosition() : "N/A", 15, labelFont, valueFont);
            addCardDetail(document, "Date of Birth:", profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : "N/A", 15, labelFont, valueFont);

            document.add(spacer);

            // QR Code on RIGHT side
            if (qrCodeImage != null && qrCodeImage.length > 0) {
                try {
                    Image qrImg = Image.getInstance(qrCodeImage);
                    qrImg.scaleToFit(65, 65);
                    qrImg.setAbsolutePosition(395, 10);
                    writer.getDirectContent().addImage(qrImg, true);
                } catch (Exception e) {
                    // skip qr on error
                }
            }

            // Barcode at bottom center
            if (barcodeImage != null && barcodeImage.length > 0) {
                try {
                    Image barcodeImg = Image.getInstance(barcodeImage);
                    barcodeImg.scaleToFit(140, 30);
                    barcodeImg.setAbsolutePosition(180, 18);
                    writer.getDirectContent().addImage(barcodeImg, true);
                } catch (Exception e) {
                    // skip barcode on error
                }
            }

            document.close();
        } catch (DocumentException e) {
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

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

            for (int i = 0; i < profiles.size(); i++) {
                Profile profile = profiles.get(i);

                if (i > 0) {
                    document.newPage();
                }

                Paragraph title = new Paragraph("ID CARD - " + profile.getProfileType().toString(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY));
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph(" "));

                // Photo in CENTER
                if (profile.getPhoto() != null && profile.getPhoto().length > 0) {
                    try {
                        Image img = Image.getInstance(profile.getPhoto());
                        img.scaleToFit(90, 110);
                        img.setAbsolutePosition(205, 120);
                        writer.getDirectContent().addImage(img, true);
                    } catch (Exception e) {
                        // skip photo on error
                    }
                }

                // Details below photo
                addCardDetail(document, "Name:", profile.getFullName(), 15, labelFont, valueFont);
                addCardDetail(document, "ID:", profile.getUniqueId(), 15, labelFont, valueFont);
                addCardDetail(document, "Dept:", profile.getDepartment(), 15, labelFont, valueFont);
                addCardDetail(document, "Position:", profile.getPosition(), 15, labelFont, valueFont);

                // QR Code on RIGHT side
                if (qrCodes != null && i < qrCodes.length && qrCodes[i] != null) {
                    try {
                        Image qrImg = Image.getInstance(qrCodes[i]);
                        qrImg.scaleToFit(70, 70);
                        qrImg.setAbsolutePosition(430, 20);
                        writer.getDirectContent().addImage(qrImg, true);
                    } catch (Exception e) {
                        // skip qr on error
                    }
                }

                // Barcode at bottom left
                if (barcodes != null && i < barcodes.length && barcodes[i] != null) {
                    try {
                        Image barcodeImg = Image.getInstance(barcodes[i]);
                        barcodeImg.scaleToFit(130, 35);
                        barcodeImg.setAbsolutePosition(20, 20);
                        writer.getDirectContent().addImage(barcodeImg, true);
                    } catch (Exception e) {
                        // skip barcode on error
                    }
                }
            }

            document.close();
        } catch (DocumentException e) {
            throw e;
        }

        return baos.toByteArray();
    }

    private void drawCardBackground(PdfContentByte canvas, float width, float height) {
        canvas.setColorFill(new BaseColor(0xF8, 0xF9, 0xFA));
        canvas.roundRectangle(3, 3, width - 6, height - 6, 8);
        canvas.fill();

        canvas.setColorStroke(new BaseColor(0xDE, 0xE2, 0xE6));
        canvas.setLineWidth(1f);
        canvas.roundRectangle(3, 3, width - 6, height - 6, 8);
        canvas.stroke();
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