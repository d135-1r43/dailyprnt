package com.dailyprnt.edition;

import com.dailyprnt.modules.Module;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test module that can be held mid-render, so a second request is guaranteed to arrive
 * while the first is still generating. Counts renders, which is how the tests tell
 * "generated once and shared" apart from "generated twice".
 */
@ApplicationScoped
public class GatedModule implements Module
{
	private final AtomicInteger renders = new AtomicInteger();
	private volatile CountDownLatch entered = new CountDownLatch(1);
	private volatile CountDownLatch released = new CountDownLatch(0);

	/** Makes the next render block until {@link #release()}. */
	public void hold()
	{
		entered = new CountDownLatch(1);
		released = new CountDownLatch(1);
	}

	/** Waits until a render is actually in progress. */
	public void awaitRenderStarted() throws InterruptedException
	{
		if (!entered.await(10, TimeUnit.SECONDS))
		{
			throw new IllegalStateException("no render started");
		}
	}

	public void release()
	{
		released.countDown();
	}

	public int renderCount()
	{
		return renders.get();
	}

	public void reset()
	{
		renders.set(0);
		entered = new CountDownLatch(1);
		released = new CountDownLatch(0);
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
		int render = renders.incrementAndGet();
		entered.countDown();
		try
		{
			released.await(10, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new IllegalStateException("interrupted while held", e);
		}
		return "<p class=\"gated\">render " + render + "</p>";
	}
}
