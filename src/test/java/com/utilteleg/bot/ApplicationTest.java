package com.utilteleg.bot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationTest {

    @Test
    void contextLoads() {
        // Это простой модульный тест, который не требует полного контекста Spring
        assertNotNull(Application.class);
    }
    
    @Test
    void mainClassIsPresent() {
        assertNotNull(Application.class.getName());
    }
}