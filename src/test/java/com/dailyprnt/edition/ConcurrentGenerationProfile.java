package com.dailyprnt.edition;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/** Runs the edition with only the gated module, so renders can be held in lockstep. */
public class ConcurrentGenerationProfile implements QuarkusTestProfile
{
	@Override
	public Map<String, String> getConfigOverrides()
	{
		return Map.of("dailyprnt.modules", "gated");
	}
}
