package com.utilteleg.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    
    public static void main(String[] args) {
        logger.info("Запуск приложения UtilTelegBot...");
        logger.info("Версия Java: {}", System.getProperty("java.version"));
        logger.info("Каталог запуска: {}", System.getProperty("user.dir"));
        logger.info("Каталог classpath: {}", System.getProperty("java.class.path"));
        
        try {
            ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
            logger.info("Приложение UtilTelegBot успешно запущено!");
            
            // Добавим диагностику конфигурации после запуска
            try {
                com.utilteleg.bot.config.AppConfig appConfig = context.getBean(com.utilteleg.bot.config.AppConfig.class);
                if (appConfig != null) {
                    logger.info("AppConfig бин успешно создан и доступен");
                    if (appConfig.getCampaigns() != null) {
                        logger.info("Загружено {} кампаний из конфигурации", appConfig.getCampaigns().size());
                    } else {
                        logger.warn("AppConfig бин создан, но список кампаний равен null");
                    }
                } else {
                    logger.error("AppConfig бин не доступен после запуска приложения");
                }
            } catch (Exception e) {
                logger.error("Ошибка при получении AppConfig бина: {}", e.getMessage(), e);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при запуске приложения UtilTelegBot: {}", e.getMessage(), e);
            throw e;
        }
    }
}