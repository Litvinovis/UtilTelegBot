package com.utilteleg.bot;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocxTextExtractionTest {

    @TempDir
    Path tempDir;

    @Test
    void testDocxTextExtraction() throws IOException {
        // Создаем временный DOCX файл с тестовым содержимым
        File docxFile = new File(tempDir.toFile(), "test.docx");
        
        // Создаем DOCX документ с тестовым содержимым
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph1 = document.createParagraph();
            XWPFRun run1 = paragraph1.createRun();
            run1.setText("Это тестовый DOCX файл");
            
            XWPFParagraph paragraph2 = document.createParagraph();
            XWPFRun run2 = paragraph2.createRun();
            run2.setText("С несколькими абзацами");
            
            // Сохраняем документ
            try (FileOutputStream out = new FileOutputStream(docxFile)) {
                document.write(out);
            }
        }
        
        // Проверяем, что файл был создан
        assertTrue(docxFile.exists());
        
        // Извлекаем текст из DOCX файла
        try (XWPFDocument document = new XWPFDocument(new java.io.FileInputStream(docxFile))) {
            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            for (XWPFParagraph paragraph : paragraphs) {
                text.append(paragraph.getText()).append("\n");
            }
            
            // Проверяем, что текст был извлечен правильно
            String extractedText = text.toString();
            assertTrue(extractedText.contains("Это тестовый DOCX файл"));
            assertTrue(extractedText.contains("С несколькими абзацами"));
        }
    }
}