package com.example.project.config;

import java.util.List;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.event.KeyValuePair;
import ch.qos.logback.classic.LoggerContext;
import com.example.project.utility.JsonUtil;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import ch.qos.logback.core.rolling.RollingFileAppender;

public class CustomFileAppender extends RollingFileAppender<ILoggingEvent>
{
	private LoggerContext context;
    private final ObjectMapper mapper = new ObjectMapper();
	
	public CustomFileAppender(LoggerContext context)
	{
		this.context = context;
	}
	
    @Override
    protected void subAppend(ILoggingEvent event) {
    	 if (!(event instanceof ILoggingEvent))  super.subAppend(event);
    	 	 
    	 ILoggingEvent newEvent = getNewEvent(event);
    	 super.subAppend(newEvent);
    }
    
    private ILoggingEvent getNewEvent(ILoggingEvent oldEvent)
    {
    	LoggingEvent newEvent = new LoggingEvent();
    	
    	newEvent = copyOldLogEventDetails(oldEvent, newEvent);
        
    	newEvent = setMDCPropertyMap(oldEvent, newEvent);
        
        boolean useFormattedMessage = false;
        
        String formattedMessage = oldEvent.getFormattedMessage();
        String[] keywords = { "HikariPool-1", "Fill pool skipped", "springdoc-openapi" };
        for (String keyword : keywords) {
            if (formattedMessage.contains(keyword)) {
            	useFormattedMessage = true; 
            	break;
            }
        }
        
        if(useFormattedMessage)
        {
        	newEvent.setMessage(formattedMessage);	
        }else 
        {
        	Object[] argumentArray = oldEvent.getArgumentArray();
            if (argumentArray != null && argumentArray.length > 0) {
            	String messageTemplate = oldEvent.getMessage();
                List<String> placeholders = extractPlaceholders(messageTemplate);
                for (int i = 0; i < argumentArray.length; i++) {
                	String argumentKey = "arg" + (i + 1);
                	if(i < placeholders.size() && !placeholders.get(i).isEmpty())
                	{
                		argumentKey = placeholders.get(i);
                        argumentKey = lowercaseFirstLetter(argumentKey);
                	}
                	  	
                    try {
                        var pair = new KeyValuePair(argumentKey, mapper.readTree(JsonUtil.toJson(argumentArray[i])));
                        newEvent.addKeyValuePair(pair);
                    } catch (Exception e) {
                    	var pair = new KeyValuePair(argumentKey, String.valueOf(argumentArray[i])); 
                    	 newEvent.addKeyValuePair(pair);
                    }
                }
                
                newEvent.setMessage(capitalizePlaceholders(messageTemplate));
            }
        }
    	
    	return newEvent;
    }

	/**
	 * @param oldEvent
	 * @param newEvent
	 */
	private LoggingEvent setMDCPropertyMap(ILoggingEvent oldEvent, LoggingEvent newEvent) {
		if (oldEvent.getMDCPropertyMap() != null) {
            var map = new Hashtable<String,String>();
        	for(var entry: oldEvent.getMDCPropertyMap().entrySet())
        	{
        		map.putIfAbsent( entry.getKey(), entry.getValue());
        	}
            newEvent.setMDCPropertyMap(map);
        }
		
		return newEvent;
	}

	/**
	 * @param oldEvent
	 * @param newEvent
	 */
	private LoggingEvent copyOldLogEventDetails(ILoggingEvent oldEvent, LoggingEvent newEvent) {
		// Copying all essential details from the old event to the new one
    	newEvent.setLoggerContext(context);
        newEvent.setLoggerName(oldEvent.getLoggerName());
        newEvent.setThreadName(oldEvent.getThreadName());
        newEvent.setLevel(oldEvent.getLevel());
        newEvent.setTimeStamp(oldEvent.getTimeStamp());
        newEvent.setArgumentArray(oldEvent.getArgumentArray());
        newEvent.setThrowableProxy((ThrowableProxy) oldEvent.getThrowableProxy());
        newEvent.setCallerData(oldEvent.getCallerData());
      
        var markersList = oldEvent.getMarkerList();
        if(markersList != null)
        {
        	for(var marker: markersList)
            {
            	  newEvent.addMarker(marker);
            }
        }
        
        return newEvent;
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

}
