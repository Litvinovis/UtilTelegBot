package com.utilteleg.bot;

import com.utilteleg.bot.model.Agency;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgencySelectionTest {

    @Test
    void testAgencyWithTemplateShowsDeliveryOptions() {
        Agency agency = new Agency("test1", "Test Agency With Template",
                "templates/test/template.txt", "templates/test/instruction.txt",
                List.of("file", "text"));

        assertTrue(agency.hasTemplate(), "Agency should have template");
        assertTrue(agency.hasInstruction(), "Agency should have instruction");
        assertFalse(agency.deliveryOptions().isEmpty(), "Delivery options should not be empty");
    }

    @Test
    void testAgencyWithoutTemplateDoesNotShowDeliveryOptions() {
        Agency agency = new Agency("test2", "Test Agency Without Template",
                null, "templates/test/instruction.txt", List.of());

        assertFalse(agency.hasTemplate(), "Agency should not have template");
        assertTrue(agency.hasInstruction(), "Agency should have instruction");
        assertTrue(agency.deliveryOptions().isEmpty(), "Delivery options should be empty");
        assertNull(agency.templateFile(), "Template file should be null");
    }

    @Test
    void testAgencyWithEmptyTemplateFileDoesNotShowDeliveryOptions() {
        Agency agency = new Agency("test3", "Test Agency With Empty Template",
                "", "templates/test/instruction.txt", List.of());

        assertFalse(agency.hasTemplate(), "Agency should not have template when template file is empty");
        assertTrue(agency.hasInstruction(), "Agency should have instruction");
    }

    @Test
    void testAgencyWithWhitespaceTemplateFileDoesNotShowDeliveryOptions() {
        Agency agency = new Agency("test4", "Test Agency With Whitespace Template",
                "   ", "templates/test/instruction.txt", List.of());

        assertFalse(agency.hasTemplate(), "Agency should not have template when template file is whitespace-only");
        assertTrue(agency.hasInstruction(), "Agency should have instruction");
    }
}
