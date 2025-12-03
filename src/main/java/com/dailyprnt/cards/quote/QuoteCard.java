package com.dailyprnt.cards.quote;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@CheckedTemplate(basePath = "com/dailyprnt/cards/quote")
public class QuoteCard
{
    public static native TemplateInstance card(Quote quote);
}
