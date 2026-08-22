package com.dailyprnt.edition;

import com.dailyprnt.modules.Module;
import com.dailyprnt.modules.ModuleRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDate;

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

	@Transactional
	public PrintedEdition editionFor(LocalDate date)
	{
		return PrintedEdition.of(repository.findByDate(date).orElseGet(() -> generate(date)));
	}

	/** Discards a stored edition so the next request regenerates it. */
	@Transactional
	public void discard(LocalDate date)
	{
		repository.findByDate(date).ifPresent(repository::delete);
	}

	private Edition generate(LocalDate date)
	{
		Edition edition = new Edition();
		edition.date = date;

		for (Module module : registry.enabled())
		{
			edition.add(render(module));
		}

		repository.persist(edition);
		return edition;
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
