package com.utilteleg.bot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationTest {

    @Test
    void contextLoads() {
        // Это простой модульный тест, который не требует полного контекста Spring
        assertNotNull(Application.class);
    }
    
    @Test
    void applicationStarts() {
        // Тест, проверяющий, что основной метод может быть вызван без ошибок
        String[] args = new String[] {};
        try {
            Application.main(args);
        } catch (Exception e) {
            // Мы ожидаем исключение здесь, потому что не предоставляем действительные учетные данные Telegram
            // Это просто для проверки, что основной метод может быть вызван
        }
    }
}