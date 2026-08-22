package com.dailyprnt.modules.weather;

/** Today's weather, shaped for printing. */
public record Weather(
		String location,
		String conditions,
		long high,
		long low,
		int precipitationProbability,
		String sunrise,
		String sunset)
{
}
