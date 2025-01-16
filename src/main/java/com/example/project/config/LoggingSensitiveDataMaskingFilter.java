package com.example.project.config;

import java.util.Map;
import org.slf4j.MDC;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class LoggingSensitiveDataMaskingFilter extends Filter<ILoggingEvent> 
{  
    private final Map<Pattern, String> sensitivePatterns;
    
    public LoggingSensitiveDataMaskingFilter() {
        this.sensitivePatterns = new HashMap<>();
        initializePatterns();
    }
    
    private void initializePatterns() {
        // Patterns with their mask formats
        addPattern("password=([^\\s&]+)", "password=*****");
        addPattern("secret=([^\\s&]+)", "secret=*****");
        addPattern("token=([^\\s&]+)", "token=*****");
        addPattern("apikey=([^\\s&]+)", "apikey=*****");
        addPattern("api_key=([^\\s&]+)", "api_key=*****");
        addPattern("authorization=([^\\s&]+)", "authorization=*****");
        addPattern("credential=([^\\s&]+)", "credential=*****");
        addPattern("\\b\\d{16}\\b", "****-****-****-****"); // Credit card
        addPattern("\\b\\d{3}-\\d{2}-\\d{4}\\b", "***-**-****"); // SSN
    }
    
    private void addPattern(String pattern, String mask) {
        sensitivePatterns.put(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE), mask);
    }
    
    @Override
    public FilterReply decide(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        
        String maskedMessage = maskSensitiveData(message);
        
        if (!maskedMessage.equals(message)) {
            MDC.put("dataMasked", "true");
        }
        
        return FilterReply.NEUTRAL;
    }
    
    private String maskSensitiveData(String message) {
        String maskedMessage = message;
        for (Map.Entry<Pattern, String> entry : sensitivePatterns.entrySet()) {
            Matcher matcher = entry.getKey().matcher(maskedMessage);
            maskedMessage = matcher.replaceAll(entry.getValue());
        }
        return maskedMessage;
    }

    public void addCustomPattern(String pattern, String mask) {
        addPattern(pattern, mask);
    }
}