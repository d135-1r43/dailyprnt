package com.dailyprnt;

import com.dailyprnt.edition.PrintedEdition;
import com.dailyprnt.edition.EditionService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Path("/daily")
public class DailyPage
{
	private static final DateTimeFormatter DATE_FORMATTER =
			DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);

	@CheckedTemplate(basePath = "")
	static class Templates
	{
		static native TemplateInstance daily(String date, PrintedEdition edition);
	}

	@Inject
	EditionService editions;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance today()
	{
		return strip(LocalDate.now());
	}

	@GET
	@Path("/{date}")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance on(@PathParam("date") String date)
	{
		return strip(parse(date));
	}

	private TemplateInstance strip(LocalDate date)
	{
		return Templates.daily(date.format(DATE_FORMATTER), editions.editionFor(date));
	}

	private static LocalDate parse(String date)
	{
		try
		{
			return LocalDate.parse(date);
		}
		catch (DateTimeParseException e)
		{
			throw new WebApplicationException(
					"Expected a date as YYYY-MM-DD but got '" + date + "'", Response.Status.BAD_REQUEST);
		}
	}
}
