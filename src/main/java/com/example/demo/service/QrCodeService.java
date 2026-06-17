package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {

    private static final int QR_CODE_SIZE = 300;
    private static final int BARCODE_WIDTH = 500;
    private static final int BARCODE_HEIGHT = 150;

    /**
     * Generate a QR Code image from the given text data
     */
    public BufferedImage generateQRCode(String text) throws WriterException {
        return generateQRCode(text, QR_CODE_SIZE);
    }

    /**
     * Generate a QR Code image with custom size
     */
    public BufferedImage generateQRCode(String text, int size) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Generate a QR Code from profile data (JSON-like string for verification)
     */
    public BufferedImage generateProfileQRCode(String uniqueId, String fullName, String profileType) throws WriterException {
        String qrData = String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"url\":\"http://localhost:8082/api/profiles/%s\"}",
                uniqueId, fullName, profileType, uniqueId
        );
        return generateQRCode(qrData);
    }

    /**
     * Generate a Code 128 barcode
     */
    public BufferedImage generateCode128Barcode(String text) throws WriterException {
        Code128Writer code128Writer = new Code128Writer();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = code128Writer.encode(text, BarcodeFormat.CODE_128, BARCODE_WIDTH, BARCODE_HEIGHT, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Generate an EAN-13 barcode
     */
    public BufferedImage generateEAN13Barcode(String text) throws WriterException {
        EAN13Writer ean13Writer = new EAN13Writer();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = ean13Writer.encode(text, BarcodeFormat.EAN_13, BARCODE_WIDTH, BARCODE_HEIGHT, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Generate barcode based on BarcodeType enum
     */
    public BufferedImage generateBarcode(String text, BarcodeType barcodeType) throws WriterException {
        return switch (barcodeType) {
            case CODE_128 -> generateCode128Barcode(text);
            case EAN_13 -> generateEAN13Barcode(text);
        };
    }
}