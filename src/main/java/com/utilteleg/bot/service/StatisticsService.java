package main.java.com.utilteleg.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatisticsService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);
    
    @Value("${statistics.file-path:data/statistics.json}")
    private String statisticsFilePath;
    
    private final Map<String, Integer> templateDownloads = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void loadStatistics() {
        try {
            File file = new File(statisticsFilePath);
            if (file.exists()) {
                MapType type = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Integer.class);
                Map<String, Integer> loadedStats = objectMapper.readValue(file, type);
                templateDownloads.putAll(loadedStats);
                logger.info("Загружено {} записей статистики из файла", loadedStats.size());
            } else {
                logger.info("Файл статистики не найден, начинаем с пустой статистики");
            }
        } catch (IOException e) {
            logger.error("Не удалось загрузить статистику из файла: {}", e.getMessage(), e);
        }
    }
    
    public void incrementTemplateDownload(String templateId) {
        templateDownloads.merge(templateId, 1, Integer::sum);
        saveStatistics();
    }
    
    public Map<String, Integer> getTemplateDownloads() {
        return Map.copyOf(templateDownloads);
    }
    
    private void saveStatistics() {
        try {
            File file = new File(statisticsFilePath);
            // Создать родительские каталоги, если они не существуют
            file.getParentFile().mkdirs();
            objectMapper.writeValue(file, templateDownloads);
        } catch (IOException e) {
            logger.error("Не удалось сохранить статистику в файл: {}", e.getMessage(), e);
        }
    }
}