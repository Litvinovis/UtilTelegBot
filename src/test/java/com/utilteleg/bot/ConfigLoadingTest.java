package com.utilteleg.bot;

import com.utilteleg.bot.config.AppConfig;
import com.utilteleg.bot.model.Campaign;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConfigLoadingTest {

    @Autowired
    private AppConfig appConfig;

    @Test
    void testConfigurationLoading() {
        assertNotNull(appConfig, "AppConfig should not be null");
        
        List<Campaign> campaigns = appConfig.getCampaigns();
        assertNotNull(campaigns, "Campaigns list should not be null");
        assertFalse(campaigns.isEmpty(), "Campaigns list should not be empty");
        assertEquals(2, campaigns.size(), "Should have 2 campaigns");
        
        // Test first campaign
        Campaign campaign1 = campaigns.get(0);
        assertEquals("campaign1", campaign1.getId());
        assertEquals("Кампания по декларации налогов", campaign1.getName());
        assertNotNull(campaign1.getAgencies());
        assertEquals(2, campaign1.getAgencies().size());
        
        // Test second campaign
        Campaign campaign2 = campaigns.get(1);
        assertEquals("campaign2", campaign2.getId());
        assertEquals("Кампания по социальным льготам", campaign2.getName());
        assertNotNull(campaign2.getAgencies());
        assertEquals(1, campaign2.getAgencies().size());
    }
}