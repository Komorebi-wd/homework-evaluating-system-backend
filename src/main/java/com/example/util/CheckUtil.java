package com.example.util;


import java.nio.charset.StandardCharsets;


public class CheckUtil {

    public static double calculateFileSimilarity(byte[] fileData1, byte[] fileData2) {
        String content1 = convertBytesToString(fileData1);
        String content2 = convertBytesToString(fileData2);

        return calculateSimilarity(content1, content2);
    }

    private static String convertBytesToString(byte[] fileData) {
        return new String(fileData, StandardCharsets.UTF_8);
        // 如果文件编码不是UTF-8，请替换为相应的编码
    }

    private static double calculateSimilarity(String content1, String content2) {
        // 在这里可以使用之前提到的相似度计算方法，如SimHash、余弦相似度等
        // 例如，使用 SimHash:
        SimHashUtil simHashUtil1 = new SimHashUtil(content1, 64);
        SimHashUtil simHashUtil2 = new SimHashUtil(content2, 64);

        return simHashUtil1.getSimilar(simHashUtil2);
    }
}
