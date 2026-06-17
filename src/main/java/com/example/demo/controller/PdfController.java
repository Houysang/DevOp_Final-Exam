package com.example.demo.controller;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.service.PdfService;
import com.example.demo.service.ProfileService;
import com.example.demo.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;
    private final ProfileService profileService;
    private final QrCodeService qrCodeService;

    @GetMapping("/generate/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        var profileOpt = profileService.getProfileById(id);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Profile profile = profileOpt.get();
        try {
            byte[] pdfBytes = pdfService.generateIdCardPdf(profile);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"id-card-" + profile.getUniqueId() + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/generate-with-qr/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> generatePdfWithQR(@PathVariable Long id,
                                                      @RequestParam(value = "barcodeType", defaultValue = "CODE_128") BarcodeType barcodeType) {
        var profileOpt = profileService.getProfileById(id);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Profile profile = profileOpt.get();
        try {
            BufferedImage qrImage = qrCodeService.generateProfileQRCode(
                    profile.getUniqueId(), profile.getFullName(), profile.getProfileType().toString());
            byte[] qrBytes = bufferedImageToBytes(qrImage, "png");

            BufferedImage barcodeImage = qrCodeService.generateBarcode(
                    profile.getUniqueId().replaceAll("[^0-9]", ""), barcodeType);
            byte[] barcodeBytes = bufferedImageToBytes(barcodeImage, "png");

            byte[] pdfBytes = pdfService.generateIdCardPdfWithQR(profile, qrBytes, barcodeBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"id-card-" + profile.getUniqueId() + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate-batch")
    @ResponseBody
    public ResponseEntity<byte[]> generateBatchPdf(@RequestBody List<Long> profileIds) {
        try {
            List<Profile> profiles = new ArrayList<>();
            List<byte[]> qrCodes = new ArrayList<>();
            List<byte[]> barcodes = new ArrayList<>();

            for (Long id : profileIds) {
                profileService.getProfileById(id).ifPresent(profile -> {
                    profiles.add(profile);
                    try {
                        BufferedImage qrImage = qrCodeService.generateProfileQRCode(
                                profile.getUniqueId(), profile.getFullName(), profile.getProfileType().toString());
                        qrCodes.add(bufferedImageToBytes(qrImage, "png"));

                        BufferedImage barcodeImage = qrCodeService.generateBarcode(
                                profile.getUniqueId().replaceAll("[^0-9]", ""), BarcodeType.CODE_128);
                        barcodes.add(bufferedImageToBytes(barcodeImage, "png"));
                    } catch (Exception e) {
                        qrCodes.add(null);
                        barcodes.add(null);
                    }
                });
            }

            byte[] pdfBytes = pdfService.generateBatchIdCardsPdf(
                    profiles, qrCodes.toArray(new byte[0][]), barcodes.toArray(new byte[0][]));
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"batch-id-cards.pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/qr/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> generateQrCodeImage(@PathVariable Long id) {
        var profileOpt = profileService.getProfileById(id);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Profile profile = profileOpt.get();
        try {
            BufferedImage qrImage = qrCodeService.generateProfileQRCode(
                    profile.getUniqueId(), profile.getFullName(), profile.getProfileType().toString());
            byte[] qrBytes = bufferedImageToBytes(qrImage, "png");
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/barcode/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> generateBarcodeImage(@PathVariable Long id,
                                                         @RequestParam(value = "type", defaultValue = "CODE_128") BarcodeType barcodeType) {
        var profileOpt = profileService.getProfileById(id);
        if (profileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Profile profile = profileOpt.get();
        try {
            BufferedImage barcodeImage = qrCodeService.generateBarcode(
                    profile.getUniqueId().replaceAll("[^0-9]", ""), barcodeType);
            byte[] barcodeBytes = bufferedImageToBytes(barcodeImage, "png");
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(barcodeBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] bufferedImageToBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
}