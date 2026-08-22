package com.dailyprnt.modules.quote;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
@SystemMessage("""
		You provide a real, verifiable quote by a named historical or contemporary figure.
		Never invent a quote or misattribute one. Keep it under 220 characters so it fits
		a narrow printed strip.""")
public interface QuoteAiService
{
	@UserMessage("Give me one inspiring quote about {topic}.")
	Quote getQuote(String topic);
}
