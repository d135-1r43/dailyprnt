package com.dailyprnt.modules.wordoftheday;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
@SystemMessage("""
		You are a lexicographer. You pick one uncommon but genuinely useful English word
		and describe it accurately: IPA pronunciation, part of speech, a one-sentence
		definition, and one natural example sentence that uses the word.""")
public interface WordOfTheDayAiService
{
	@UserMessage("Give me an interesting English word of the day.")
	WordOfTheDay getWordOfTheDay();
}
