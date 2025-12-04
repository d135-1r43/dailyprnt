package com.dailyprnt.cards.wordoftheday;

import com.dailyprnt.cards.Card;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@CheckedTemplate(basePath = "com/dailyprnt/cards/wordoftheday")
public class WordOfTheDayCard implements Card
{
	public static native TemplateInstance card(WordOfTheDay word);

	@Inject
	WordOfTheDayAiService wordOfTheDayAiService;

	@Override
	public String renderCard()
	{
		WordOfTheDay word = wordOfTheDayAiService.getWordOfTheDay();
		return card(word).render();
	}
}
