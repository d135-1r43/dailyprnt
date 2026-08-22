package com.dailyprnt.modules.woodcut;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** The gpt-image models always return base64 rather than a URL. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeneratedImages(List<Item> data)
{
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Item(@JsonProperty("b64_json") String b64Json)
	{
	}
}
