package com.dailyprnt.modules.weather;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Open-Meteo forecast API. Free for non-commercial use and needs no API key.
 */
@Path("/v1/forecast")
@RegisterRestClient(configKey = "open-meteo")
public interface OpenMeteoClient
{
	@GET
	Forecast daily(
			@QueryParam("latitude") double latitude,
			@QueryParam("longitude") double longitude,
			@QueryParam("daily") String daily,
			@QueryParam("timezone") String timezone,
			@QueryParam("forecast_days") int forecastDays);
}
