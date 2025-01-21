package com.example.project.config;

import java.util.HashMap;
import java.util.Map;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class LoggingMetadataEnrichmentFilter extends Filter<ILoggingEvent> {

    private final String environment;

    public LoggingMetadataEnrichmentFilter(String environment) {
        this.environment = environment;
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event == null) {
            return FilterReply.NEUTRAL;
        }

        Map<String, Object> metadata = new HashMap<>(); 
        metadata.put("env", environment);
        metadata.put("thread", Thread.currentThread().getName());

        metadata.forEach((key, value) -> {
            if (value != null) {
                org.slf4j.MDC.put(key, value.toString());
            }
        });

        return FilterReply.NEUTRAL;
    }
}
