package com.dailyprnt.edition;

import com.dailyprnt.modules.Module;
import jakarta.enterprise.context.ApplicationScoped;

/** Test module that always blows up, standing in for a dead upstream API. */
@ApplicationScoped
public class FailingModule implements Module
{
	@Override
	public String id()
	{
		return "failing";
	}

	@Override
	public String title()
	{
		return "Failing";
	}

	@Override
	public String render()
	{
		throw new IllegalStateException("upstream is down");
	}
}
