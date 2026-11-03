package cn.sycoder.utils;

import cn.sycoder.exception.BusinessException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class BarcodeUtils {

    private static final int DEFAULT_BARCODE_WIDTH = 420;
    private static final int DEFAULT_BARCODE_HEIGHT = 110;
    private static final int DEFAULT_PADDING = 16;
    private static final int DEFAULT_TEXT_HEIGHT = 24;

    private BarcodeUtils() {
    }

    public static String buildBarcodeContent(Long id, String code) {
        if (id == null) {
            throw new BusinessException("产品ID不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException("产品编码不能为空");
        }
        return id + "-" + code.trim();
    }

    public static BarcodePayload parseBarcodeContent(String content) {
        String value = content == null ? "" : content.trim();
        if (value.isEmpty()) {
            return new BarcodePayload(null, "");
        }
        int separatorIndex = value.indexOf('-');
        if (separatorIndex > 0) {
            String idPart = value.substring(0, separatorIndex).trim();
            String codePart = value.substring(separatorIndex + 1).trim();
            if (idPart.matches("\\d+") && !codePart.isEmpty()) {
                return new BarcodePayload(Long.valueOf(idPart), codePart);
            }
        }
        if (value.matches("\\d+")) {
            return new BarcodePayload(Long.valueOf(value), value);
        }
        return new BarcodePayload(null, value);
    }

    public static BufferedImage generateCode128Image(String content) {
        return generateCode128Image(content, DEFAULT_BARCODE_WIDTH, DEFAULT_BARCODE_HEIGHT);
    }

    public static BufferedImage generateCode128Image(String content, int width, int height) {
        validateContent(content);
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, width, height, hints);
            BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return appendText(barcodeImage, content);
        } catch (WriterException e) {
            throw new BusinessException("条形码生成失败");
        }
    }

    public static void writeCode128(String content, OutputStream outputStream) throws IOException {
        BufferedImage image = generateCode128Image(content);
        ImageIO.write(image, "png", outputStream);
    }

    public static String decode(BufferedImage image) {
        if (image == null) {
            throw new BusinessException("图片不能为空");
        }
        try {
            Map<DecodeHintType, Object> hints = new HashMap<>();
            Result result = new MultiFormatReader().decode(
                    new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image))),
                    hints
            );
            return result.getText();
        } catch (NotFoundException e) {
            throw new BusinessException("未识别到条形码内容");
        }
    }

    public static String decode(InputStream inputStream) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        return decode(image);
    }

    private static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("条形码内容不能为空");
        }
    }

    private static BufferedImage appendText(BufferedImage barcodeImage, String content) {
        int width = barcodeImage.getWidth() + DEFAULT_PADDING * 2;
        int height = barcodeImage.getHeight() + DEFAULT_PADDING * 2 + DEFAULT_TEXT_HEIGHT;
        BufferedImage mergedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = mergedImage.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(barcodeImage, DEFAULT_PADDING, DEFAULT_PADDING, null);

            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("SansSerif", Font.PLAIN, 16));
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fontMetrics = graphics.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(content);
            int textX = Math.max((width - textWidth) / 2, DEFAULT_PADDING);
            int textY = barcodeImage.getHeight() + DEFAULT_PADDING * 2 + 18;
            graphics.drawString(content, textX, textY);
            return mergedImage;
        } finally {
            graphics.dispose();
        }
    }

    public static final class BarcodePayload {
        private final Long id;
        private final String code;

        public BarcodePayload(Long id, String code) {
            this.id = id;
            this.code = code;
        }

        public Long getId() {
            return id;
        }

        public String getCode() {
            return code;
        }
    }
}
