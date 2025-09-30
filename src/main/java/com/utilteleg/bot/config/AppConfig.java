package com.utilteleg.bot.config;

import com.utilteleg.bot.model.Campaign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "")
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    
    private List<Campaign> campaigns;
    
    public AppConfig() {
        logger.info("Создание бина AppConfig...");
    }

    // Getters and Setters
    public List<Campaign> getCampaigns() {
        logger.debug("Получение списка кампаний. Количество кампаний: {}", campaigns != null ? campaigns.size() : "null");
        if (campaigns == null) {
            logger.error("Список кампаний равен null. Конфигурация не была загружена правильно.");
            logger.error("Проверьте, что файл application.yml существует в каталоге src/main/resources/ и имеет правильный формат.");
            logger.error("Также проверьте, что префикс 'campaigns' указан правильно в файле конфигурации.");
        } else if (campaigns.isEmpty()) {
            logger.warn("Список кампаний пуст. Проверьте файл конфигурации application.yml.");
        }
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        logger.info("Установка списка кампаний. Новое количество кампаний: {}", campaigns != null ? campaigns.size() : "null");
        if (campaigns != null) {
            logger.info("Детали загруженных кампаний:");
            for (int i = 0; i < campaigns.size(); i++) {
                Campaign campaign = campaigns.get(i);
                logger.info("  Кампания {}: id='{}', name='{}', количество органов={}", 
                    i+1, 
                    campaign.getId(), 
                    campaign.getName(),
                    campaign.getAgencies() != null ? campaign.getAgencies().size() : "null");
                
                if (campaign.getAgencies() != null) {
                    for (int j = 0; j < campaign.getAgencies().size(); j++) {
                        com.utilteleg.bot.model.Agency agency = campaign.getAgencies().get(j);
                        logger.info("    Орган {}: id='{}', name='{}', template-file='{}', варианты доставки={}",
                            j+1,
                            agency.getId(),
                            agency.getName(),
                            agency.getTemplateFile(),
                            agency.getDeliveryOptions() != null ? agency.getDeliveryOptions().size() : "null");
                    }
                }
            }
        } else {
            logger.warn("Попытка установить null в качестве списка кампаний");
        }
        this.campaigns = campaigns;
    }
    
    // Добавим метод для получения информации о состоянии конфигурации
    public String getConfigurationStatus() {
        if (campaigns == null) {
            return "Конфигурация не загружена (campaigns = null)";
        } else {
            return "Конфигурация загружена успешно. Количество кампаний: " + campaigns.size();
        }
    }
}