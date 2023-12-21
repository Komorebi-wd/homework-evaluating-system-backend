package com.example.util;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import javax.imageio.ImageIO;


public class CheckUtil {

    // 修改后的calculateFileSimilarity方法，包含文件类型参数
    public static double calculateFileSimilarity(byte[] fileData1, String type1, byte[] fileData2, String type2) {
        String content1 = extractText(fileData1, type1);
        String content2 = extractText(fileData2, type2);

        return calculateSimilarity(content1, content2);
    }

    // 根据文件类型提取文本内容
    private static String extractText(byte[] fileData, String fileType) {
        return switch (fileType) {
            case "pdf" -> extractTextFromPDF(fileData);
            case "docx" -> extractTextFromWord(fileData);
            case "txt" -> convertBytesToString(fileData);
//            case "png", "jpg" -> extractTextFromImage(fileData);
            default -> convertBytesToString(fileData);
        };
    }

    private static String convertBytesToString(byte[] fileData) {
        return new String(fileData, StandardCharsets.UTF_8);
        // 如果文件编码不是UTF-8，请替换为相应的编码
    }

    public static String extractTextFromPDF(byte[] pdfData) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfData))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String extractTextFromWord(byte[] wordData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(wordData);
             XWPFDocument document = new XWPFDocument(bais)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

//    public static String extractTextFromImage(byte[] imageData) {
//        Tesseract tesseract = new Tesseract();
//        try {
//            tesseract.setDatapath("src/tessdata"); // 确保tessdata目录在你的项目路径中
//            tesseract.setLanguage("eng"); // 设置使用的语言，例如 "eng"
//
//            return tesseract.doOCR(ImageIO.read(new ByteArrayInputStream(imageData)));
//        } catch (TesseractException | IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    //对于String类型的文本，校验相似度
    private static double calculateSimilarity(String content1, String content2) {
        // 在这里可以使用之前提到的相似度计算方法，如SimHash、余弦相似度等
        // 例如，使用 SimHash:
        SimHashUtil simHashUtil1 = new SimHashUtil(content1, 64);
        SimHashUtil simHashUtil2 = new SimHashUtil(content2, 64);

        return simHashUtil1.getSimilar(simHashUtil2);
    }
}
