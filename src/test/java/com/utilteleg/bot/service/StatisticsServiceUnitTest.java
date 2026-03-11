package com.utilteleg.bot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsServiceUnitTest {

    @TempDir
    Path tempDir;

    @Test
    void incrementTemplateDownload_persistsStatisticsFile() throws Exception {
        StatisticsService service = new StatisticsService();

        Field pathField = StatisticsService.class.getDeclaredField("statisticsFilePath");
        pathField.setAccessible(true);
        Path stats = tempDir.resolve("stats").resolve("statistics.json");
        pathField.set(service, stats.toString());

        service.loadStatistics();
        service.incrementTemplateDownload("tpl1");
        service.incrementTemplateDownload("tpl1");

        assertTrue(Files.exists(stats));
        assertEquals(2, service.getTemplateDownloads().get("tpl1"));
    }
}
