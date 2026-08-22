package com.dailyprnt.edition;

import com.dailyprnt.modules.Module;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Test module that holds every caller at a barrier until the expected number of
 * concurrent renders have arrived, so a race is forced rather than hoped for.
 */
@ApplicationScoped
public class GatedModule implements Module
{
	private volatile CyclicBarrier barrier;

	public void expectConcurrentRenders(int parties)
	{
		barrier = new CyclicBarrier(parties);
	}

	@Override
	public String id()
	{
		return "gated";
	}

	@Override
	public String title()
	{
		return "Gated";
	}

	@Override
	public String render()
	{
		CyclicBarrier gate = barrier;
		if (gate != null)
		{
			try
			{
				gate.await(10, TimeUnit.SECONDS);
			}
			catch (InterruptedException | BrokenBarrierException | TimeoutException e)
			{
				throw new IllegalStateException("gate was never reached by all renderers", e);
			}
		}
		return "<p class=\"gated\">gated</p>";
	}
}
