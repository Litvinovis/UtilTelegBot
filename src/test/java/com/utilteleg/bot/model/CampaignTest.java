package com.utilteleg.bot.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CampaignTest {

    @Test
    void constructorsAndAccessors_workAsExpected() {
        Agency agency = new Agency("a1", "Agency", "t.txt", "i.txt", List.of("file"));
        Campaign campaign = new Campaign("c1", "Campaign", List.of(agency));

        assertEquals("c1", campaign.getId());
        assertEquals("Campaign", campaign.getName());
        assertEquals(1, campaign.getAgencies().size());

        campaign.setId("c2");
        campaign.setName("Campaign2");
        campaign.setAgencies(List.of());

        assertEquals("c2", campaign.getId());
        assertEquals("Campaign2", campaign.getName());
        assertTrue(campaign.getAgencies().isEmpty());
    }
}
