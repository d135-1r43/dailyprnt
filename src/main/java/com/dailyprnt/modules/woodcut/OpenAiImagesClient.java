package com.dailyprnt.modules.woodcut;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * OpenAI image generation. LangChain4j's image model targets the older DALL-E request
 * shape, so this calls the API directly to reach the gpt-image models and their quality
 * settings.
 */
@Path("/v1/images/generations")
@RegisterRestClient(configKey = "openai-images")
@ClientHeaderParam(name = "Authorization", value = "{authorization}")
public interface OpenAiImagesClient
{
	@POST
	GeneratedImages generate(ImageRequest request);

	/** Reuses the key the AI services already run on, so there is only one to configure. */
	default String authorization()
	{
		return "Bearer " + ConfigProvider.getConfig()
				.getValue("quarkus.langchain4j.openai.api-key", String.class);
	}
}
