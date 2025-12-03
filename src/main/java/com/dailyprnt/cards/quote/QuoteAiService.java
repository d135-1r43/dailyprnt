package com.dailyprnt.cards.quote;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
@SystemMessage("You provide a real and verified inspirational quote on a given topic")
public interface QuoteAiService
{
	@UserMessage("What is the inspirational quote on {topic} today?")
	Quote getQuote(String topic);
}
