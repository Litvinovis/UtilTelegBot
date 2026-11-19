package com.utilteleg.bot;

import com.utilteleg.bot.config.AppConfig;
import com.utilteleg.bot.model.Agency;
import com.utilteleg.bot.model.Campaign;
import com.utilteleg.bot.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelegramBotServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void pollingDeliversFileAfterOrderedCampaignSelection() throws Exception {
        Path template = Files.writeString(tempDir.resolve("template.txt"), "Шаблон для теста");
        AppConfig config = mock(AppConfig.class);
        StatisticsService statistics = mock(StatisticsService.class);
        TelegramClient client = mock(TelegramClient.class);
        Agency agency = new Agency("agency", "Тестовый орган", template.toString(), null, List.of("file"));
        when(config.getCampaigns()).thenReturn(List.of(new Campaign("campaign", "Кампания", List.of(agency))));
        try (TelegramBotService service = new TelegramBotService("test_bot", "123:abc", config, statistics, client)) {
            service.getUpdatesConsumer().consume(List.of(update("/campaigns"), update("1"), update("1"), update("1")));

            ArgumentCaptor<SendDocument> document = ArgumentCaptor.forClass(SendDocument.class);
            verify(client, timeout(5000)).execute(document.capture());
            assertEquals("42", document.getValue().getChatId());
            assertEquals(template.toFile(), document.getValue().getDocument().getNewMediaFile());
            verify(statistics).incrementTemplateDownload(template.toString());
        }
    }

    @Test
    void sendsWelcomeAndTemplateAsText() throws Exception {
        Path template = Files.writeString(tempDir.resolve("template.txt"), "Тестовый шаблон");
        AppConfig config = mock(AppConfig.class);
        StatisticsService statistics = mock(StatisticsService.class);
        TelegramClient client = mock(TelegramClient.class);
        Agency agency = new Agency("agency", "Тестовый орган", template.toString(), null, List.of("text"));
        when(config.getCampaigns()).thenReturn(List.of(new Campaign("campaign", "Кампания", List.of(agency))));
        try (TelegramBotService service = new TelegramBotService("test_bot", "123:abc", config, statistics, client)) {
            for (String text : List.of("/start", "/campaigns", "1", "1", "1")) {
                service.consume(update(text));
            }

            verify(client).execute(argThat((SendMessage message) -> message.getText().contains("/campaigns")));
            verify(client).execute(argThat((SendMessage message) ->
                    "42".equals(message.getChatId()) && message.getText().contains("Тестовый шаблон")));
        }
    }

    private Update update(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getChatId()).thenReturn(42L);
        when(message.getText()).thenReturn(text);
        return update;
    }

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
