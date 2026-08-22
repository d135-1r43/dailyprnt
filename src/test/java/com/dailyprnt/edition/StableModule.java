package com.dailyprnt.edition;

import com.dailyprnt.modules.Module;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test module whose output changes on every render, so a repeated edition proves the
 * stored copy was replayed rather than regenerated.
 */
@ApplicationScoped
public class StableModule implements Module
{
	private final AtomicInteger renders = new AtomicInteger();

	@Override
	public String id()
	{
		return "stable";
	}

	@Override
	public String title()
	{
		return "Stable";
	}

	@Override
	public String render()
	{
		return "<p class=\"counted\">render " + renders.incrementAndGet() + "</p>";
	}

	public int renderCount()
	{
		return renders.get();
	}

	public void reset()
	{
		renders.set(0);
	}
}
