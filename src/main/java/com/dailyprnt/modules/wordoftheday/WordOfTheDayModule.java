package com.dailyprnt.modules.wordoftheday;

import com.dailyprnt.modules.Module;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@CheckedTemplate(basePath = "com/dailyprnt/modules/wordoftheday")
public class WordOfTheDayModule implements Module
{
	public static native TemplateInstance module(WordOfTheDay word);

	@Inject
	WordOfTheDayAiService wordOfTheDayAiService;

	@Override
	public String id()
	{
		return "wordoftheday";
	}

	@Override
	public String title()
	{
		return "Word of the Day";
	}

	@Override
	public String render()
	{
		return module(wordOfTheDayAiService.getWordOfTheDay()).render();
	}
}
