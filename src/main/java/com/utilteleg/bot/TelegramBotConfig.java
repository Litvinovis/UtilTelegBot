package com.utilteleg.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilteleg.bot.config.AppConfig;
import com.utilteleg.bot.service.StatisticsService;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory.DefaultOkHttpClientCreator;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class TelegramBotConfig {
    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    // TelegramBots закрывает сессию, но не останавливает её executor.
    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService telegramPollingExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Bean
    public TelegramBotsLongPollingApplication telegramPollingApplication(ScheduledExecutorService telegramPollingExecutor) {
        return new TelegramBotsLongPollingApplication(ObjectMapper::new, new DefaultOkHttpClientCreator(),
                () -> telegramPollingExecutor);
    }

    // Отправка файлов использует потоки диспетчера OkHttp.
    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService telegramHttpExecutor() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public TelegramClient telegramClient(@Qualifier("telegramHttpExecutor") ExecutorService telegramHttpExecutor) {
        OkHttpClient client = new OkHttpClient.Builder()
                .dispatcher(new Dispatcher(telegramHttpExecutor))
                .build();
        return new OkHttpTelegramClient(client, botToken);
    }

    @Bean
    public TelegramBotService telegramBotService(AppConfig appConfig, StatisticsService statisticsService,
                                                  TelegramClient telegramClient) {
        return new TelegramBotService(botUsername, botToken, appConfig, statisticsService, telegramClient);
    }
}
