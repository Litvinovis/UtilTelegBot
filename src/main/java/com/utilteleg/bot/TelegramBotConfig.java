package com.utilteleg.bot;

import com.utilteleg.bot.config.AppConfig;
import com.utilteleg.bot.service.StatisticsService;
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
    public TelegramBotService telegramBotService(AppConfig appConfig, StatisticsService statisticsService) {
        return new TelegramBotService(botUsername, botToken, appConfig, statisticsService);
    }
    
    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(telegramBotService);
            logger.info("Бот успешно зарегистрирован: @{}", botUsername);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при регистрации бота: {}", e.getMessage(), e);
            logger.error("Проверьте, что токен бота и имя пользователя указаны правильно (application.yml / env переменные TELEGRAM_BOT_TOKEN, TELEGRAM_BOT_USERNAME)");
            throw e;
        }
        return botsApi;
    }
}