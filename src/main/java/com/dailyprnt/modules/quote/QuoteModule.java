package com.dailyprnt.modules.quote;

import com.dailyprnt.modules.Module;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@CheckedTemplate(basePath = "com/dailyprnt/modules/quote")
public class QuoteModule implements Module
{
	public static native TemplateInstance module(Quote quote);

	@Inject
	QuoteAiService quoteAiService;

	@ConfigProperty(name = "dailyprnt.quote.topic", defaultValue = "stoicism")
	String topic;

	@Override
	public String id()
	{
		return "quote";
	}

	@Override
	public String title()
	{
		return "Quote of the Day";
	}

	@Override
	public String render()
	{
		return module(quoteAiService.getQuote(topic)).render();
	}
}
