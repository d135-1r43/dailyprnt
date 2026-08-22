package com.dailyprnt.edition;

import com.dailyprnt.edition.PrintedEdition.PrintedBlock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EditionServiceTest
{
	private static final LocalDate DATE = LocalDate.of(2026, 8, 22);

	@Inject
	EditionService editions;

	@Inject
	EditionRepository repository;

	@Inject
	StableModule stableModule;

	@BeforeEach
	void resetCounter()
	{
		stableModule.reset();
	}

	@AfterEach
	@Transactional
	void cleanup()
	{
		// A bulk delete does not cascade to blocks, so remove each edition individually.
		repository.findAll().list().forEach(repository::delete);
	}

	@Test
	void shouldPrintEnabledModulesInConfiguredOrder()
	{
		// given
		// dailyprnt.modules is "stable,failing" for the test profile

		// when
		PrintedEdition edition = editions.editionFor(DATE);

		// then
		assertEquals(List.of("stable", "failing"), edition.blocks().stream().map(PrintedBlock::moduleId).toList());
	}

	@Test
	void shouldGenerateEachDateOnlyOnce()
	{
		// given
		editions.editionFor(DATE);

		// when
		PrintedEdition replayed = editions.editionFor(DATE);

		// then
		assertEquals(1, stableModule.renderCount(), "module was re-rendered instead of replayed");
		assertTrue(replayed.blocks().get(0).html().contains("render 1"));
	}

	@Test
	void shouldGenerateSeparateEditionsPerDate()
	{
		// given
		editions.editionFor(DATE);

		// when
		editions.editionFor(DATE.plusDays(1));

		// then
		assertEquals(2, stableModule.renderCount());
		assertEquals(2, repository.count());
	}

	@Test
	void shouldReplaceAFailingModuleWithAPlaceholder()
	{
		// when
		PrintedEdition edition = editions.editionFor(DATE);

		// then
		PrintedBlock failed = edition.blocks().get(1);
		assertTrue(failed.failed());
		assertTrue(failed.html().contains("Not available today."));
	}

	@Test
	void shouldKeepTheRestOfTheEditionWhenOneModuleFails()
	{
		// when
		PrintedEdition edition = editions.editionFor(DATE);

		// then
		PrintedBlock healthy = edition.blocks().get(0);
		assertFalse(healthy.failed());
		assertTrue(healthy.html().contains("render 1"));
	}

	@Test
	void shouldRegenerateAfterTheEditionIsDiscarded()
	{
		// given
		editions.editionFor(DATE);

		// when
		editions.discard(DATE);
		PrintedEdition regenerated = editions.editionFor(DATE);

		// then
		assertEquals(2, stableModule.renderCount());
		assertTrue(regenerated.blocks().get(0).html().contains("render 2"));
	}
}
