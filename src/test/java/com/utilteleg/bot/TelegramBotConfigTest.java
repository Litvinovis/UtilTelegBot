package com.utilteleg.bot;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TelegramBotConfigTest {

    @Test
    void telegramBotServiceBean_creationWorks() {
        TelegramBotConfig cfg = new TelegramBotConfig();
        ReflectionTestUtils.setField(cfg, "botUsername", "test_bot");
        ReflectionTestUtils.setField(cfg, "botToken", "123:abc");

        TelegramBotService service = cfg.telegramBotService();
        assertNotNull(service);
        assertEquals("test_bot", service.getBotUsername());
        assertEquals("123:abc", service.getBotToken());
    }
}
