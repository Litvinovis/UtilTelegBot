package com.utilteleg.bot;

import com.utilteleg.bot.config.AppConfig;
import com.utilteleg.bot.model.Campaign;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        assertEquals(3, campaign2.getAgencies().size());
        
        // Test agency with no template but with instruction
        com.utilteleg.bot.model.Agency agency5 = campaign2.getAgencies().get(2);
        assertEquals("agency5", agency5.getId());
        assertEquals("Принять участие в публичном обсуждении", agency5.getName());
        assertFalse(agency5.hasTemplate(), "Agency should not have template");
        assertTrue(agency5.hasInstruction(), "Agency should have instruction");
        assertNotNull(agency5.getDeliveryOptions());
        assertTrue(agency5.getDeliveryOptions().isEmpty(), "Delivery options should be empty");
    }
    
    @Test
    void testAgencyWithoutTemplate() {
        assertNotNull(appConfig, "AppConfig should not be null");
        
        List<Campaign> campaigns = appConfig.getCampaigns();
        assertNotNull(campaigns, "Campaigns list should not be null");
        assertFalse(campaigns.isEmpty(), "Campaigns list should not be empty");
        
        // Find the agency with no template
        Campaign campaign2 = campaigns.get(1);
        com.utilteleg.bot.model.Agency agency = campaign2.getAgencies().get(2); // "Принять участие в публичном обсуждении"
        
        // Verify it has no template but has instruction
        assertFalse(agency.hasTemplate(), "Agency should not have template");
        assertTrue(agency.hasInstruction(), "Agency should have instruction");
        
        // Verify delivery options are empty
        assertNotNull(agency.getDeliveryOptions());
        assertTrue(agency.getDeliveryOptions().isEmpty(), "Delivery options should be empty");
        
        // Verify template file is null
        assertNull(agency.getTemplateFile(), "Template file should be null");
        
        // Verify hasTemplate() returns false
        assertFalse(agency.hasTemplate(), "hasTemplate() should return false");
        
        // Verify hasInstruction() returns true
        assertTrue(agency.hasInstruction(), "hasInstruction() should return true");
    }
}