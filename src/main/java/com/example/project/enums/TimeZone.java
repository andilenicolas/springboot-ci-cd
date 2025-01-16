package com.example.project.enums;

import lombok.Getter;

@Getter
public enum TimeZone 
{
	UTC("UTC"),
	EST("America/New_York"),
	CST("America/Chicago"),
	MST("America/Denver"),
	PST("America/Los_Angeles");

	private final String zoneId;
	
	TimeZone(String zoneId) {
	   this.zoneId = zoneId;
	}
}
