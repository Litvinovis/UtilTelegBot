package com.utilteleg.bot;

import com.utilteleg.bot.model.Agency;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class AgencySelectionTest {

    @Test
    void testAgencyWithTemplateShowsDeliveryOptions() {
        // Create an agency with a template
        Agency agency = new Agency();
        agency.setId("test1");
        agency.setName("Test Agency With Template");
        agency.setTemplateFile("templates/test/template.txt");
        agency.setInstructionFile("templates/test/instruction.txt");
        agency.setDeliveryOptions(Arrays.asList("file", "text"));
        
        // Verify the agency has a template
        assertTrue(agency.hasTemplate(), "Agency should have template");
        assertTrue(agency.hasInstruction(), "Agency should have instruction");
        assertFalse(agency.getDeliveryOptions().isEmpty(), "Delivery options should not be empty");
    }
    
    @Test
    void testAgencyWithoutTemplateDoesNotShowDeliveryOptions() {
        // Create an agency without a template
        Agency agency = new Agency();
        agency.setId("test2");
        agency.setName("Test Agency Without Template");
        agency.setTemplateFile(null); // No template file
        agency.setInstructionFile("templates/test/instruction.txt");
        agency.setDeliveryOptions(Collections.emptyList()); // No delivery options
        
        // Verify the agency does not have a template
        assertFalse(agency.hasTemplate(), "Agency should not have template");
        assertTrue(agency.hasInstruction(), "Agency should have instruction");
        assertTrue(agency.getDeliveryOptions().isEmpty(), "Delivery options should be empty");
        assertNull(agency.getTemplateFile(), "Template file should be null");
    }
    
    @Test
    void testAgencyWithEmptyTemplateFileDoesNotShowDeliveryOptions() {
        // Create an agency with an empty template file
        Agency agency = new Agency();
        agency.setId("test3");
        agency.setName("Test Agency With Empty Template");
        agency.setTemplateFile(""); // Empty template file
        agency.setInstructionFile("templates/test/instruction.txt");
        agency.setDeliveryOptions(Collections.emptyList());
        
        // Verify the agency does not have a template
        assertFalse(agency.hasTemplate(), "Agency should not have template when template file is empty");
        assertTrue(agency.hasInstruction(), "Agency should have instruction");
    }
    
    @Test
    void testAgencyWithWhitespaceTemplateFileDoesNotShowDeliveryOptions() {
        // Create an agency with a whitespace-only template file
        Agency agency = new Agency();
        agency.setId("test4");
        agency.setName("Test Agency With Whitespace Template");
        agency.setTemplateFile("   "); // Whitespace-only template file
        agency.setInstructionFile("templates/test/instruction.txt");
        agency.setDeliveryOptions(Collections.emptyList());
        
        // Verify the agency does not have a template
        assertFalse(agency.hasTemplate(), "Agency should not have template when template file is whitespace-only");
        assertTrue(agency.hasInstruction(), "Agency should have instruction");
    }
}