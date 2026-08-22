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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

	private final ConcurrentMap<LocalDate, Object> generationLocks = new ConcurrentHashMap<>();

	public PrintedEdition editionFor(LocalDate date)
	{
		PrintedEdition stored = stored(date);
		if (stored != null)
		{
			return stored;
		}

		// Generating costs a paid image and the better part of a minute, so only one
		// request per date does the work. The rest wait here and then read what it stored,
		// rather than each paying to generate the same day over again.
		synchronized (lockFor(date))
		{
			PrintedEdition arrived = stored(date);
			return arrived != null ? arrived : generate(date);
		}
	}

	/**
	 * One monitor per date, kept for the life of the process. Dropping it once generation
	 * finished would let a waiting request take a fresh monitor and generate in parallel
	 * after all, and the entries are a handful of bytes each.
	 */
	private Object lockFor(LocalDate date)
	{
		return generationLocks.computeIfAbsent(date, ignored -> new Object());
	}

	private PrintedEdition generate(LocalDate date)
	{
		// Modules call slow upstream APIs, so render before opening a transaction rather
		// than holding a database connection for the duration.
		List<EditionBlock> blocks = renderAll();

		try
		{
			return QuarkusTransaction.requiringNew().call(() -> persist(date, blocks));
		}
		catch (RuntimeException e)
		{
			// The lock above only covers this process, so a second instance can still get
			// there first. The unique constraint on the date is what makes that detectable;
			// if a stored edition exists now, that instance won and its edition is the one
			// to print.
			PrintedEdition winner = stored(date);
			if (winner == null)
			{
				throw e;
			}
			LOG.warnf("Another instance stored the edition for %s first, replaying it", date);
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
