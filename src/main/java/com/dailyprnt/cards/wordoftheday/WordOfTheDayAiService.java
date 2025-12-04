package com.dailyprnt.cards.wordoftheday;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
@SystemMessage("You provide a daily inspirational word of the day in english")
public interface WordOfTheDayAiService
{
	WordOfTheDay getWordOfTheDay();
}
