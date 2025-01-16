package com.example.project.service.timeZoneService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class TimeZoneService implements ITimeZoneService
{
	private static final String DEFAULT_TIMEZONE = "UTC";
	private static final String TIMEZONE_HEADER = "X-Timezone";

	public ZoneId getCurrentTimeZone() {
	   ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
	   if (attributes == null) {
		   return ZoneId.systemDefault();
	   }	   
	   
	   String timezone = attributes.getRequest().getHeader(TIMEZONE_HEADER);
       if (timezone == null || timezone.isEmpty()) {
    	   return ZoneId.systemDefault();
       }

       
       try {
           return ZoneId.of(timezone);
       } catch (Exception e) {
           return ZoneId.of(DEFAULT_TIMEZONE);
       }
	}

	public LocalDateTime convertToUserTimeZone(LocalDateTime utcDateTime) {
	   if (utcDateTime == null) {
	       return null;
	   }
	   
	   ZoneId userTimeZone = getCurrentTimeZone();
	   
	   return ZonedDateTime.of(utcDateTime, ZoneId.of("UTC"))
		      .withZoneSameInstant(userTimeZone)
		      .toLocalDateTime();
	}
}

