package com.utilteleg.bot;

import com.utilteleg.bot.config.AppConfig;
import com.utilteleg.bot.model.Agency;
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

        Campaign campaign1 = campaigns.get(0);
        assertEquals("campaign1", campaign1.id());
        assertEquals("Кампания по декларации налогов", campaign1.name());
        assertNotNull(campaign1.agencies());
        assertEquals(2, campaign1.agencies().size());

        Campaign campaign2 = campaigns.get(1);
        assertEquals("campaign2", campaign2.id());
        assertEquals("Кампания по социальным льготам", campaign2.name());
        assertNotNull(campaign2.agencies());
        assertEquals(3, campaign2.agencies().size());

        Agency agency5 = campaign2.agencies().get(2);
        assertEquals("agency5", agency5.id());
        assertEquals("Принять участие в публичном обсуждении", agency5.name());
        assertFalse(agency5.hasTemplate(), "Agency should not have template");
        assertTrue(agency5.hasInstruction(), "Agency should have instruction");
        assertNotNull(agency5.deliveryOptions());
        assertTrue(agency5.deliveryOptions().isEmpty(), "Delivery options should be empty");
    }

    @Test
    void testAgencyWithoutTemplate() {
        assertNotNull(appConfig, "AppConfig should not be null");

        List<Campaign> campaigns = appConfig.getCampaigns();
        assertNotNull(campaigns, "Campaigns list should not be null");
        assertFalse(campaigns.isEmpty(), "Campaigns list should not be empty");

        Campaign campaign2 = campaigns.get(1);
        Agency agency = campaign2.agencies().get(2);

        assertFalse(agency.hasTemplate(), "Agency should not have template");
        assertTrue(agency.hasInstruction(), "Agency should have instruction");
        assertNotNull(agency.deliveryOptions());
        assertTrue(agency.deliveryOptions().isEmpty(), "Delivery options should be empty");
        assertNull(agency.templateFile(), "Template file should be null");
        assertFalse(agency.hasTemplate(), "hasTemplate() should return false");
        assertTrue(agency.hasInstruction(), "hasInstruction() should return true");
    }
}
