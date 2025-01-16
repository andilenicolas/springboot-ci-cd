package com.example.project.config;

import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import ch.qos.logback.core.AppenderBase;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.project.utility.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SeqHttpAppender extends AppenderBase<ILoggingEvent> 
{
    private String url;
    private Encoder<ILoggingEvent> encoder;
    private final BlockingQueue<ILoggingEvent> queue = new LinkedBlockingQueue<>(1000);
    private final ObjectMapper mapper = new ObjectMapper();
    private Thread workerThread;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    @Override
    public void start() {
        if (url == null || url.isEmpty()) {
            addError("URL for SeqHttpAppender is not set.");
            return;
        }

        if (encoder == null) {
            addError("Encoder for SeqHttpAppender is not set.");
            return;
        }

        super.start();

        workerThread = new Thread(this::processQueue, "SeqHttpAppender-Worker");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    @Override
    public void stop() {
        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(5000); // Wait for the worker to stop gracefully.
            } catch (InterruptedException ignored) {
            }
        }
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted() || eventObject == null) {
            return;
        }
        if (!queue.offer(eventObject)) {
            addWarn("Queue is full. Dropping log event: " + eventObject.getMessage());
        }
    }

    private void processQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ArrayNode eventsNode = mapper.createArrayNode();
                ILoggingEvent event;
                int batchSize = 0;

                while ((event = queue.poll()) != null && batchSize < 100) {
                    eventsNode.add(serializeEvent(event));
                    batchSize++;
                }

                if (batchSize > 0) {
                    ObjectNode payload = mapper.createObjectNode();
                    payload.set("Events", eventsNode);
                    sendToSeq(payload.toString());
                }

                Thread.sleep(1000); // Flush logs every second.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Exit on interruption.
            } catch (Exception e) {
                addError("Error processing log queue", e);
            }
        }
    }

	
    private ObjectNode serializeEvent(ILoggingEvent event) {
        ObjectNode eventNode = mapper.createObjectNode();

        // Format timestamp
        Instant instant = Instant.ofEpochMilli(event.getTimeStamp());
        String timestamp = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'")
                .withZone(ZoneOffset.UTC)
                .format(instant);
        eventNode.put("Timestamp", timestamp);
        
        boolean useFormattedMessage = false;
        
        String formattedMessage = event.getFormattedMessage();
        String[] keywords = { "HikariPool-1", "Fill pool skipped", "springdoc-openapi" };
        for (String keyword : keywords) {
            if (formattedMessage.contains(keyword)) {
            	useFormattedMessage = true; 
            	break;
            }
        }
        
        if(useFormattedMessage)
        {
            eventNode.put("MessageTemplate", formattedMessage);	
        }else
        {
            // Extract MessageTemplate
            String messageTemplate = event.getMessage();

            // Add MDC properties to Properties field
            ObjectNode propertiesNode = eventNode.putObject("Properties");
            event.getMDCPropertyMap().forEach((key, value) -> {
                try {
                    propertiesNode.set(key, mapper.readTree(value));
                } catch (Exception e) {
                    propertiesNode.put(key, value);
                }
            });

            // Handle Arguments and map to placeholders
            Object[] argumentArray = event.getArgumentArray();
            if (argumentArray != null && argumentArray.length > 0) {
                List<String> placeholders = extractPlaceholders(messageTemplate);
                for (int i = 0; i < argumentArray.length; i++) {
                	String argumentKey = "arg" + (i + 1);
                	if(i < placeholders.size() && !placeholders.get(i).isEmpty())
                	{
                		argumentKey = placeholders.get(i);
                        argumentKey = lowercaseFirstLetter(argumentKey);
                	}
                	  	
                    try {
                        propertiesNode.set(argumentKey, mapper.readTree(JsonUtil.toJson(argumentArray[i])));
                    } catch (Exception e) {
                        propertiesNode.put(argumentKey, String.valueOf(argumentArray[i])); // Fallback to string representation
                    }
                }
            }

            // Update MessageTemplate
            eventNode.put("MessageTemplate", capitalizePlaceholders(messageTemplate));	
        }

        // Add the log level
        eventNode.put("Level", event.getLevel().toString());
        
        eventNode.put("LoggerName", event.getLoggerName());
        
        return eventNode;
    }

    // Helper function to extract placeholders from the MessageTemplate
    private List<String> extractPlaceholders(String messageTemplate) {
        List<String> placeholders = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([^}]*)\\}"); // Matches {key}
        Matcher matcher = pattern.matcher(messageTemplate);
        while (matcher.find()) {
            placeholders.add(matcher.group(1)); // Add the placeholder name (without curly braces)
        }
        return placeholders;
    }
    
    private String capitalizePlaceholders(String messageTemplate) {
        List<String> placeholders = extractPlaceholders(messageTemplate);
        for(String placeholder: placeholders)
        {
        	String newPlaceholder = capitalizeFirstLetter(placeholder);
        	if(!newPlaceholder.isEmpty())
        	{        		
        		messageTemplate.replace(placeholder, newPlaceholder);
        	}
        }
        return messageTemplate;
    }

	
	// Helper function to capitalize the first letter
	private String capitalizeFirstLetter(String word) {
		if (word == null || word.isEmpty()) {
			return "";
		}
	    return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
	}
	
	private String lowercaseFirstLetter(String word) {
		if (word == null || word.isEmpty()) {
			return "";
		}
	    return word.substring(0, 1).toLowerCase() + word.substring(1).toLowerCase();
	}


    private void sendToSeq(String json) {
        try {
            @SuppressWarnings("deprecation")
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Charset", "utf-8");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                addWarn("Seq responded with status code: " + responseCode);
            }
            
            connection.disconnect();
        } catch (Exception e) {
            addError("Failed to send logs to Seq", e);
        }
    }

}
