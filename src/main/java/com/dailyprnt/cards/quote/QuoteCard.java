package com.dailyprnt.cards.quote;

import com.dailyprnt.cards.Card;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@CheckedTemplate(basePath = "com/dailyprnt/cards/quote")
@ApplicationScoped
public class QuoteCard implements Card
{
	public static native TemplateInstance card(Quote quote);

	@Inject
	QuoteAiService quoteAiService;

	@Override
	public String renderCard()
	{
		Quote quote = quoteAiService.getQuote("stoicism");
		return card(quote).render();
	}
}
