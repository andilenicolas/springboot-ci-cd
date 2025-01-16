package com.example.project.service.timeZoneService;

import java.time.ZoneId;
import java.time.LocalDateTime;

public interface ITimeZoneService 
{
	ZoneId getCurrentTimeZone();
	LocalDateTime convertToUserTimeZone(LocalDateTime utcDateTime);
}
