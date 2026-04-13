package com.utilteleg.bot;

import com.utilteleg.bot.config.AppConfig;
import com.utilteleg.bot.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TelegramBotConfigTest {

    @Test
    void telegramBotServiceBean_creationWorks() {
        TelegramBotConfig cfg = new TelegramBotConfig();
        ReflectionTestUtils.setField(cfg, "botUsername", "test_bot");
        ReflectionTestUtils.setField(cfg, "botToken", "123:abc");

        AppConfig appConfig = Mockito.mock(AppConfig.class);
        StatisticsService statisticsService = Mockito.mock(StatisticsService.class);

        TelegramBotService service = cfg.telegramBotService(appConfig, statisticsService);
        assertNotNull(service);
        assertEquals("test_bot", service.getBotUsername());
        assertEquals("123:abc", service.getBotToken());
    }
}
