package com.dailyprnt.edition;

import java.time.LocalDate;
import java.util.List;

/**
 * A fully materialised edition, detached from persistence so that rendering never
 * touches a lazy association outside its transaction.
 */
public record PrintedEdition(LocalDate date, List<PrintedBlock> blocks)
{
	public record PrintedBlock(String moduleId, String title, String html, boolean failed)
	{
	}

	static PrintedEdition of(Edition edition)
	{
		return new PrintedEdition(
				edition.date,
				edition.blocks.stream()
						.map(block -> new PrintedBlock(block.moduleId, block.title, block.html, block.failed))
						.toList());
	}
}
