package com.dailyprnt.modules.woodcut;

/** Request body for the OpenAI image generation API. */
public record ImageRequest(String model, String prompt, String size, String quality, int n)
{
}
