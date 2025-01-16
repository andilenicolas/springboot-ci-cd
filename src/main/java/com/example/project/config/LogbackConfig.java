package com.example.project.config;

import java.time.LocalDate;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.annotation.PostConstruct;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.encoder.LogstashEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import org.springframework.beans.factory.annotation.Value;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import org.springframework.context.annotation.Configuration;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;

@Configuration
public class LogbackConfig 
{
    @Value("${app.seq.logs.server.url}")
    private String seqLogsServerUrl;
    
    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @PostConstruct
    public void configureLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        String logPath = "logs/" + activeProfile;

        // Console Appender
        ConsoleAppender<ILoggingEvent> consoleAppender = createConsoleAppender(context);
        consoleAppender.start();

        // File Appender
        RollingFileAppender<ILoggingEvent> fileAppender = createFileAppender(context, logPath);
        fileAppender.start();

        // Seq Appender
        SeqHttpAppender seqAppender = createSeqAppender(context);
        seqAppender.start();

        // Async Appender
        MultiplexingAsyncAppender asyncAppender = new MultiplexingAsyncAppender();
        asyncAppender.setContext(context);
        asyncAppender.setName("AsyncAppender");
        asyncAppender.addAppender(fileAppender);
        asyncAppender.addAppender(seqAppender);
        asyncAppender.start();
        
        // Logger Configuration
        configureLogger(context, "org.springframework", Level.INFO, consoleAppender);
        configureLogger(context, "com.zaxxer.hikari", Level.DEBUG, consoleAppender);
        configureLogger(context, "org.springframework.web.servlet.handler", Level.DEBUG, consoleAppender);
        configureLogger(context, "org.hibernate", Level.DEBUG, consoleAppender);
        configureLogger(context, "org.hibernate.SQL", Level.DEBUG, consoleAppender);
        configureLogger(context, "com.example.project", Level.DEBUG, asyncAppender);

        // Root Logger
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(asyncAppender);
    }

    private ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext context) {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("Console");
        appender.setEncoder(createPatternEncoder(context, "%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n"));
        addLoggingFilters(appender);
        return appender;
    }

    private RollingFileAppender<ILoggingEvent> createFileAppender(LoggerContext context, String logPath) {
    	CustomFileAppender appender = new CustomFileAppender(context);
        appender.setContext(context);
        appender.setName("File");
        appender.setFile(logPath + "/app-" + LocalDate.now() + ".log"); // Initial file name
        
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(appender);
        rollingPolicy.setFileNamePattern(logPath + "/app-%d{yyyy-MM-dd}-%i.log"); // Adds counter
        rollingPolicy.setMaxFileSize( FileSize.valueOf("25MB")); // Trigger new file when size exceeds limit
        rollingPolicy.setMaxHistory(7); // Retain logs for 7 days
        rollingPolicy.setTotalSizeCap(ch.qos.logback.core.util.FileSize.valueOf("1GB"));
        rollingPolicy.start();

        appender.setRollingPolicy(rollingPolicy);
        appender.setEncoder(createLogstashEncoder(context));
        addLoggingFilters(appender);
        return appender;
    }


    private SeqHttpAppender createSeqAppender(LoggerContext context) {
        SeqHttpAppender appender = new SeqHttpAppender();
        appender.setContext(context);
        appender.setName("Seq");
        appender.setUrl(seqLogsServerUrl);
        appender.setEncoder(createLogstashEncoder(context));
        addLoggingFilters(appender);
        return appender;
    }

    private PatternLayoutEncoder createPatternEncoder(LoggerContext context, String pattern) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(pattern);
        encoder.start();
        return encoder;
    }

    private LogstashEncoder createLogstashEncoder(LoggerContext context) {
        LogstashEncoder encoder = new LogstashEncoder();
        encoder.setContext(context);
        encoder.setIncludeContext(true); // Include MDC context in logs
        encoder.setIncludeStructuredArguments(true);
        encoder.setCustomFields("{\"app_name\":\"" + appName + "\",\"environment\":\"" + activeProfile + "\"}");
        encoder.start();
        return encoder;
    }

    private void configureLogger(LoggerContext context, String loggerName, Level level, ch.qos.logback.core.Appender<ILoggingEvent> appender) {
        Logger logger = context.getLogger(loggerName);
        logger.setLevel(level);
        logger.addAppender(appender);
        logger.setAdditive(false); // Prevents logs from propagating to the root logger
    }
    
	private void addLoggingFilters(FilterAttachable<ILoggingEvent> appender) {
		LoggingMetadataEnrichmentFilter enrichmentfilter = new LoggingMetadataEnrichmentFilter(activeProfile);
		appender.addFilter(enrichmentfilter);
		 
	    LoggingSensitiveDataMaskingFilter maskingFilter = new LoggingSensitiveDataMaskingFilter();
	    appender.addFilter(maskingFilter);
	}
}
