package com.utilteleg.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke test that verifies application.yml can be parsed and contains expected campaign data.
 * Replaces the former TestConfigLoading debug utility that was incorrectly placed in src/main/.
 */
class TestConfigLoadingSmokeTest {

    @SuppressWarnings("unchecked")
    @Test
    void applicationYmlIsLoadableAndContainsCampaigns() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            assertNotNull(is, "application.yml must be present on the classpath");
            Map<String, Object> yamlData = mapper.readValue(is, Map.class);
            assertNotNull(yamlData, "YAML data should not be null");

            List<Map<String, Object>> campaigns = (List<Map<String, Object>>) yamlData.get("campaigns");
            assertNotNull(campaigns, "campaigns section must be present");
            assertFalse(campaigns.isEmpty(), "campaigns list must not be empty");

            for (Map<String, Object> campaign : campaigns) {
                assertNotNull(campaign.get("name"), "Each campaign must have a name");
                List<Map<String, Object>> agencies = (List<Map<String, Object>>) campaign.get("agencies");
                assertNotNull(agencies, "Each campaign must have agencies");
            }
        }
    }
}
