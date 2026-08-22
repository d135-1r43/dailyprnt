package com.dailyprnt.edition;

import com.dailyprnt.modules.Module;
import com.dailyprnt.modules.ModuleRegistry;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates an edition once per date and replays it thereafter.
 */
@ApplicationScoped
public class EditionService
{
	private static final Logger LOG = Logger.getLogger(EditionService.class);

	@Inject
	EditionRepository repository;

	@Inject
	ModuleRegistry registry;

	public PrintedEdition editionFor(LocalDate date)
	{
		PrintedEdition stored = stored(date);
		if (stored != null)
		{
			return stored;
		}

		// Modules call slow upstream APIs, so render before opening a transaction rather
		// than holding a database connection for the duration.
		List<EditionBlock> blocks = renderAll();

		try
		{
			return QuarkusTransaction.requiringNew().call(() -> persist(date, blocks));
		}
		catch (RuntimeException e)
		{
			// A concurrent request may have generated the same date first. The unique
			// constraint on the date is what makes that collision detectable; if a stored
			// edition exists now, that request won and its edition is the one to print.
			PrintedEdition winner = stored(date);
			if (winner == null)
			{
				throw e;
			}
			LOG.debugf("Lost the race to generate %s, replaying the stored edition", date);
			return winner;
		}
	}

	/** Discards a stored edition so the next request regenerates it. */
	@Transactional
	public void discard(LocalDate date)
	{
		repository.findByDate(date).ifPresent(repository::delete);
	}

	private PrintedEdition stored(LocalDate date)
	{
		return QuarkusTransaction.requiringNew()
				.call(() -> repository.findByDate(date).map(PrintedEdition::of).orElse(null));
	}

	private PrintedEdition persist(LocalDate date, List<EditionBlock> blocks)
	{
		Edition edition = new Edition();
		edition.date = date;
		blocks.forEach(edition::add);
		repository.persist(edition);
		return PrintedEdition.of(edition);
	}

	private List<EditionBlock> renderAll()
	{
		List<EditionBlock> blocks = new ArrayList<>();
		for (Module module : registry.enabled())
		{
			blocks.add(render(module));
		}
		return blocks;
	}

	/**
	 * Renders one module, converting a failure into a placeholder block so that a single
	 * broken module cannot take the whole edition down.
	 */
	private EditionBlock render(Module module)
	{
		try
		{
			return EditionBlock.of(module.id(), module.title(), module.render(), false);
		}
		catch (RuntimeException e)
		{
			LOG.errorf(e, "Module '%s' failed; printing a placeholder instead", module.id());
			return EditionBlock.of(module.id(), module.title(), placeholder(), true);
		}
	}

	private String placeholder()
	{
		return "<p class=\"unavailable\">Not available today.</p>";
	}
}
