package com.utilteleg.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBotConfig.class);
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Bean
    public TelegramBotService telegramBotService() {
        return new TelegramBotService(botUsername, botToken);
    }
    
    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(telegramBotService);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при регистрации бота: {}", e.getMessage());
            logger.error("Проверьте, что токен бота и имя пользователя указаны правильно в application.yml");
            // Попробуем продолжить работу, даже если регистрация не удалась
            // Это позволит приложению запуститься, даже если бот не работает
            logger.info("Приложение будет запущено без регистрации бота. Бот не будет работать до исправления конфигурации.");
        }
        return botsApi;
    }
}