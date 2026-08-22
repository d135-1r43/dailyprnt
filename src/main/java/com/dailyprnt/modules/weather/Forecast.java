package com.dailyprnt.modules.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Raw Open-Meteo response. Every daily field is a parallel array indexed by day. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Forecast(Daily daily)
{
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Daily(
			@JsonProperty("weather_code") List<Integer> weatherCode,
			@JsonProperty("temperature_2m_max") List<Double> temperatureMax,
			@JsonProperty("temperature_2m_min") List<Double> temperatureMin,
			@JsonProperty("precipitation_probability_max") List<Integer> precipitationProbability,
			@JsonProperty("sunrise") List<String> sunrise,
			@JsonProperty("sunset") List<String> sunset)
	{
	}
}
