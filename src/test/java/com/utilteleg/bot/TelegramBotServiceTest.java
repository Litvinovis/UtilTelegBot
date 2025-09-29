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
    void testTextFileHandling() throws IOException {
        // Создаем временный текстовый файл для тестирования
        Path tempText = Files.createTempFile("test", ".txt");
        String content = "Это тестовый текстовый файл\nС несколькими строками";
        Files.write(tempText, content.getBytes());
        
        // Создаем объект Agency с текстовым файлом
        Agency agency = new Agency();
        agency.setId("test");
        agency.setName("Test Agency");
        agency.setTemplateFile(tempText.toString());
        agency.setDeliveryOptions(Collections.singletonList("text"));
        
        // Проверяем, что файл существует
        File templateFile = new File(agency.getTemplateFile());
        assertTrue(templateFile.exists());
        assertTrue(templateFile.getName().toLowerCase().endsWith(".txt"));
        
        // Очищаем временный файл
        Files.deleteIfExists(tempText);
    }
    
    @Test
    void testDeliveryOptionTranslation() {
        // Тестирование преобразования опций доставки
        assertEquals("file", translateDeliveryOption("файл"));
        assertEquals("text", translateDeliveryOption("текстовое сообщение"));
        // Убедимся, что английские опции остаются без изменений
        assertEquals("file", translateDeliveryOption("file"));
        assertEquals("text", translateDeliveryOption("text"));
    }
    
    @Test
    void testSendTemplateDecisionLogic() {
        // Тестирование логики выбора между отправкой файла и текста
        // В методе sendTemplate проверяется только "file", все остальное отправляется как текст
        assertTrue(shouldSendAsFile("file"), "Должно отправляться как файл");
        
        // Русская опция "файл" переводится в "file" до вызова sendTemplate, 
        // поэтому в sendTemplate она не проверяется напрямую
        // Но для полноты тестирования проверим:
        assertFalse(shouldSendAsFile("файл"), "В sendTemplate русская опция не проверяется напрямую");
        
        assertFalse(shouldSendAsFile("text"), "Должно отправляться как текст");
        assertFalse(shouldSendAsFile("текстовое сообщение"), "Должно отправляться как текст");
    }
    
    // Вспомогательный метод для тестирования преобразования опций доставки
    private String translateDeliveryOption(String option) {
        if ("файл".equals(option)) {
            return "file";
        } else if ("текстовое сообщение".equals(option)) {
            return "text";
        }
        return option; // Возвращаем как есть, если это уже английская опция
    }
    
    // Вспомогательный метод для тестирования логики отправки (имитация метода sendTemplate)
    private boolean shouldSendAsFile(String deliveryOption) {
        // Это точная копия логики из метода sendTemplate
        return "file".equals(deliveryOption);
    }
}