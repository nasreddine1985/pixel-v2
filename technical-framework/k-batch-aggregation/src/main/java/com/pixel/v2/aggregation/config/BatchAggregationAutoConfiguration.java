package com.pixel.v2.aggregation.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for k-batch-aggregation kamelet components
 * Enables component scanning for aggregation strategies and related beans
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.pixel.v2.aggregation")
public class BatchAggregationAutoConfiguration {
}