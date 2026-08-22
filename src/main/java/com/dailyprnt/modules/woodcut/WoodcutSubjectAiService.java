package com.dailyprnt.modules.woodcut;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
@SystemMessage("""
		You choose the subject of a daily woodcut illustration for a printed strip.

		Pick one concrete, visually striking subject that reads clearly as a black and
		white print: a single animal, plant, tool, building or simple landscape. Avoid
		faces, crowds, lettering and busy scenes, which lose all legibility in a small
		one-colour print.

		'title' is a short caption in title case, at most four words. 'scene' is one
		descriptive sentence naming the subject and its setting, with no mention of style,
		colour or medium.""")
public interface WoodcutSubjectAiService
{
	@UserMessage("Choose the subject of today's woodcut on the theme of {theme}.")
	WoodcutSubject propose(String theme);
}
