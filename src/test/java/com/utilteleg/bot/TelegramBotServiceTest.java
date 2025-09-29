package com.utilteleg.bot;

import com.utilteleg.bot.model.Agency;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TelegramBotServiceTest {

    @Test
    void testDeliveryOptionValues() {
        // Тестирование значений опций доставки
        Agency agency = new Agency();
        agency.setId("test");
        agency.setName("Test Agency");
        agency.setDeliveryOptions(Arrays.asList("file", "text"));
        
        // Проверяем, что опции корректно установлены
        assertEquals(2, agency.getDeliveryOptions().size());
        assertEquals("file", agency.getDeliveryOptions().get(0));
        assertEquals("text", agency.getDeliveryOptions().get(1));
    }
    
    @Test
    void testDocxFileHandling() throws IOException {
        // Создаем временный DOCX файл для тестирования
        Path tempDocx = Files.createTempFile("test", ".docx");
        Files.write(tempDocx, "Это тестовый DOCX файл".getBytes());
        
        // Создаем объект Agency с DOCX файлом
        Agency agency = new Agency();
        agency.setId("test");
        agency.setName("Test Agency");
        agency.setTemplateFile(tempDocx.toString());
        agency.setDeliveryOptions(Collections.singletonList("file"));
        
        // Проверяем, что файл существует
        File templateFile = new File(agency.getTemplateFile());
        assertTrue(templateFile.exists());
        assertTrue(templateFile.getName().toLowerCase().endsWith(".docx"));
        
        // Очищаем временный файл
        Files.deleteIfExists(tempDocx);
    }
}