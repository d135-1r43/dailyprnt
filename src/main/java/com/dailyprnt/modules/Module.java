package com.dailyprnt.modules;

/**
 * One block of content in a daily edition. Implementations are discovered via CDI and
 * selected by {@code dailyprnt.modules}, which also fixes their order on the strip.
 */
public interface Module
{
	/** Stable identifier used in configuration and persisted with the rendered block. */
	String id();

	/** Label printed above the block. */
	String title();

	/** Strip markup for today's content. May throw; the edition isolates the failure. */
	String render();
}
