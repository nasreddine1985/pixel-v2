package com.pixel.v2.flowpacs008;

import com.pixel.v2.flowpacs008.config.TestJmsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJmsConfig.class)
class FlowPacs008ApplicationTests {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }

}
