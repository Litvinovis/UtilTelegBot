package com.utilteleg.bot.config;

import com.utilteleg.bot.model.Agency;
import com.utilteleg.bot.model.Campaign;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigUnitTest {

    @Test
    void configurationStatus_reflectsLoadedState() {
        AppConfig cfg = new AppConfig();
        assertTrue(cfg.getConfigurationStatus().contains("не загружена"));

        Agency agency = new Agency("a1", "Agency", "t.txt", "i.txt", List.of("file"));
        Campaign campaign = new Campaign("c1", "Campaign", List.of(agency));
        cfg.setCampaigns(List.of(campaign));

        assertNotNull(cfg.getCampaigns());
        assertEquals(1, cfg.getCampaigns().size());
        assertTrue(cfg.getConfigurationStatus().contains("Количество кампаний: 1"));
    }
}
