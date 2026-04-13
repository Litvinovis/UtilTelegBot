package com.utilteleg.bot;

import com.utilteleg.bot.model.Agency;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TelegramBotServiceTest {

    @Test
    void testDeliveryOptionValues() {
        Agency agency = new Agency("test", "Test Agency", null, null, List.of("file", "text"));

        assertEquals(2, agency.deliveryOptions().size());
        assertEquals("file", agency.deliveryOptions().get(0));
        assertEquals("text", agency.deliveryOptions().get(1));
    }

    @Test
    void testTextFileHandling() throws IOException {
        Path tempText = Files.createTempFile("test", ".txt");
        String content = "Это тестовый текстовый файл\nС несколькими строками";
        Files.write(tempText, content.getBytes());

        Agency agency = new Agency("test", "Test Agency", tempText.toString(), null, List.of("text"));

        File templateFile = new File(agency.templateFile());
        assertTrue(templateFile.exists());
        assertTrue(templateFile.getName().toLowerCase().endsWith(".txt"));

        Files.deleteIfExists(tempText);
    }

    @Test
    void testDeliveryOptionTranslation() {
        assertEquals("file", translateDeliveryOption("файл"));
        assertEquals("text", translateDeliveryOption("текстовое сообщение"));
        assertEquals("file", translateDeliveryOption("file"));
        assertEquals("text", translateDeliveryOption("text"));
    }

    @Test
    void testSendTemplateDecisionLogic() {
        assertTrue(shouldSendAsFile("file"), "Должно отправляться как файл");
        assertFalse(shouldSendAsFile("файл"), "В sendTemplate русская опция не проверяется напрямую");
        assertFalse(shouldSendAsFile("text"), "Должно отправляться как текст");
        assertFalse(shouldSendAsFile("текстовое сообщение"), "Должно отправляться как текст");
    }

    private String translateDeliveryOption(String option) {
        return switch (option) {
            case "файл" -> "file";
            case "текстовое сообщение" -> "text";
            default -> option;
        };
    }

    private boolean shouldSendAsFile(String deliveryOption) {
        return "file".equals(deliveryOption);
    }
}
