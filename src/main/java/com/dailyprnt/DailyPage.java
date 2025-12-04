package com.dailyprnt;

import com.dailyprnt.cards.quote.Quote;
import com.dailyprnt.cards.quote.QuoteCard;
import com.dailyprnt.cards.wordoftheday.WordOfTheDay;
import com.dailyprnt.cards.wordoftheday.WordOfTheDayCard;
import io.quarkus.qute.RawString;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNull;

@Path("/daily")
public class DailyPage
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    private final Template daily;

	@Inject
	QuoteCard quoteCard;

	@Inject
	WordOfTheDayCard wordOfTheDayCard;

    public DailyPage(Template daily)
    {
        this.daily = requireNonNull(daily, "daily is required");
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get()
    {
        WordOfTheDay word = new WordOfTheDay(
                "Serendipity",
                "/ˌser.ənˈdɪp.ə.ti/",
                "noun",
                "The occurrence of events by chance in a happy or beneficial way.",
                "The discovery was a serendipity that changed the course of science."
        );

        String formattedDate = LocalDate.now().format(DATE_FORMATTER);

        return daily
                .data("date", formattedDate)
                .data("quoteCard", new RawString(quoteCard.renderCard()))
                .data("wordCard", new RawString(wordOfTheDayCard.renderCard()));
    }
}
