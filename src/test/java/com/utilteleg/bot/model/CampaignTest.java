package com.utilteleg.bot.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CampaignTest {

    @Test
    void constructorsAndAccessors_workAsExpected() {
        Agency agency = new Agency("a1", "Agency", "t.txt", "i.txt", List.of("file"));
        Campaign campaign = new Campaign("c1", "Campaign", List.of(agency));

        assertEquals("c1", campaign.id());
        assertEquals("Campaign", campaign.name());
        assertEquals(1, campaign.agencies().size());
    }

    @Test
    void emptyAgencies_workAsExpected() {
        Campaign campaign = new Campaign("c2", "Campaign2", List.of());

        assertEquals("c2", campaign.id());
        assertEquals("Campaign2", campaign.name());
        assertTrue(campaign.agencies().isEmpty());
    }
}
