package com.dailyprnt.modules.weather;

import com.dailyprnt.modules.Module;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
@CheckedTemplate(basePath = "com/dailyprnt/modules/weather")
public class WeatherModule implements Module
{
	private static final String DAILY_FIELDS =
			"weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset";

	public static native TemplateInstance module(Weather weather);

	@Inject
	@RestClient
	OpenMeteoClient openMeteo;

	@ConfigProperty(name = "dailyprnt.weather.latitude")
	double latitude;

	@ConfigProperty(name = "dailyprnt.weather.longitude")
	double longitude;

	@ConfigProperty(name = "dailyprnt.weather.location")
	String location;

	@ConfigProperty(name = "dailyprnt.weather.timezone", defaultValue = "auto")
	String timezone;

	@Override
	public String id()
	{
		return "weather";
	}

	@Override
	public String title()
	{
		return "Weather";
	}

	@Override
	public String render()
	{
		Forecast.Daily daily = openMeteo.daily(latitude, longitude, DAILY_FIELDS, timezone, 1).daily();
		Weather weather = new Weather(
				location,
				WeatherCodes.describe(first(daily.weatherCode())),
				Math.round(first(daily.temperatureMax())),
				Math.round(first(daily.temperatureMin())),
				first(daily.precipitationProbability()),
				time(first(daily.sunrise())),
				time(first(daily.sunset())));
		return module(weather).render();
	}

	private static <T> T first(List<T> values)
	{
		if (values == null || values.isEmpty())
		{
			throw new IllegalStateException("Open-Meteo returned no value for today");
		}
		return values.get(0);
	}

	/** Open-Meteo returns ISO local date-times; the strip only shows the clock time. */
	private static String time(String isoDateTime)
	{
		return isoDateTime.substring(isoDateTime.indexOf('T') + 1);
	}
}
