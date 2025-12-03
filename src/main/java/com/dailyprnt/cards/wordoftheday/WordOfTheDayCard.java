package com.dailyprnt.cards.wordoftheday;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@CheckedTemplate(basePath = "com/dailyprnt/cards/wordoftheday")
public class WordOfTheDayCard
{
    public static native TemplateInstance card(WordOfTheDay word);
}
