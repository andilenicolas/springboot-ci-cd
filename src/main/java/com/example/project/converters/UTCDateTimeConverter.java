package com.example.project.converters;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;
import com.example.project.core.SpringContext;
import com.example.project.service.timeZoneService.ITimeZoneService;

@Converter(autoApply = true)
public class UTCDateTimeConverter implements AttributeConverter<LocalDateTime, LocalDateTime> 
{
	private ITimeZoneService getTimeZoneService() {
        return SpringContext.getBean(ITimeZoneService.class);
    }
	 
	@Override
	public LocalDateTime convertToDatabaseColumn(LocalDateTime localDateTime) {
	   if (localDateTime == null) {
	       return null;
	   }
	   
	   return ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
	         .withZoneSameInstant(ZoneId.of("UTC"))
	         .toLocalDateTime();
	}
	
	@Override
	public LocalDateTime convertToEntityAttribute(LocalDateTime utcDateTime) 
	{
		if (utcDateTime == null){
			return null;
		}
	    
	    ZoneId targetZone = ZoneId.systemDefault();
        try 
        {
            ITimeZoneService timeZoneService = getTimeZoneService();
   
            if (timeZoneService != null) {
                targetZone = timeZoneService.getCurrentTimeZone();
            }
        } catch (Exception e) {}
	        
	     return ZonedDateTime.of(utcDateTime, ZoneId.of("UTC"))
	            .withZoneSameInstant(targetZone)
	            .toLocalDateTime();
	 }
}

