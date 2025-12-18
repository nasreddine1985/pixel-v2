package com.pixel.v2.referentiel.controller;

import com.pixel.v2.referentiel.service.ConfigurationService;
import com.pixel.v2.referentiel.service.RefFlowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigurationController.class)
class ConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigurationService configurationService;

    @MockBean
    private RefFlowService refFlowService;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("referentiel-service"));
    }

    @Test
    void testInfoEndpoint() throws Exception {
        // Mock the getAllSupportedFlows method
        when(configurationService.getAllSupportedFlows())
                .thenReturn(java.util.Set.of("CH", "FR", "DE"));

        mockMvc.perform(get("/api/info")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("PIXEL-V2 Referentiel Service"))
                .andExpect(jsonPath("$.version").value("1.0.1-SNAPSHOT"));
    }
}
