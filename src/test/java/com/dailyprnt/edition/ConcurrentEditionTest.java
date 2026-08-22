package com.dailyprnt.edition;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Two requests for the same date arriving together must not both generate it. Generating
 * costs a paid image generation, so duplicating the work is not merely wasteful, and the
 * duplicate insert used to violate the unique constraint on the date.
 */
@QuarkusTest
@TestProfile(ConcurrentGenerationProfile.class)
class ConcurrentEditionTest
{
	private static final LocalDate DATE = LocalDate.of(2026, 8, 22);

	@Inject
	EditionService editions;

	@Inject
	EditionRepository repository;

	@Inject
	GatedModule gatedModule;

	@BeforeEach
	void resetModule()
	{
		gatedModule.reset();
	}

	@AfterEach
	@Transactional
	void cleanup()
	{
		repository.findAll().list().forEach(repository::delete);
	}

	@Test
	void shouldGenerateOnlyOnceWhenASecondRequestArrivesMidGeneration() throws Exception
	{
		// given one request held inside render, so the second is guaranteed to arrive
		// while the first is still generating
		gatedModule.hold();
		ExecutorService pool = Executors.newFixedThreadPool(2);
		Future<PrintedEdition> first = pool.submit(() -> editions.editionFor(DATE));
		gatedModule.awaitRenderStarted();

		// when a second request asks for the same date
		Future<PrintedEdition> second = pool.submit(() -> editions.editionFor(DATE));
		gatedModule.release();
		PrintedEdition a = first.get(30, TimeUnit.SECONDS);
		PrintedEdition b = second.get(30, TimeUnit.SECONDS);
		pool.shutdown();

		// then the work was done once and shared, not paid for twice
		assertEquals(1, gatedModule.renderCount(), "the edition was generated more than once");
		assertEquals(1, repository.count());
		assertEquals(html(a), html(b));
		assertTrue(html(a).contains("render 1"));
	}

	@Test
	void shouldReplayTheStoredEditionOnceGenerationHasFinished() throws Exception
	{
		// given a date generated while another request waited
		gatedModule.hold();
		ExecutorService pool = Executors.newFixedThreadPool(2);
		Future<PrintedEdition> first = pool.submit(() -> editions.editionFor(DATE));
		gatedModule.awaitRenderStarted();
		gatedModule.release();
		String generated = html(first.get(30, TimeUnit.SECONDS));
		pool.shutdown();

		// when a later request asks for it
		PrintedEdition replayed = editions.editionFor(DATE);

		// then
		assertEquals(1, gatedModule.renderCount());
		assertEquals(generated, html(replayed));
	}

	@Test
	void shouldFallBackToTheStoredEditionWhenAnotherInstanceInsertsFirst() throws Exception
	{
		// given a request held mid-generation, and another instance storing the same date
		// before it gets to persist. The in-process lock cannot see that instance, so the
		// unique constraint is what catches it.
		gatedModule.hold();
		ExecutorService pool = Executors.newFixedThreadPool(1);
		Future<PrintedEdition> request = pool.submit(() -> editions.editionFor(DATE));
		gatedModule.awaitRenderStarted();
		storeCompetingEdition();

		// when the held request resumes and its insert collides
		gatedModule.release();
		PrintedEdition result = request.get(30, TimeUnit.SECONDS);
		pool.shutdown();

		// then it prints the edition that was already stored rather than failing
		assertEquals(1, repository.count());
		assertTrue(html(result).contains("from another instance"));
	}

	/** Writes an edition for the date directly, standing in for a second instance. */
	private void storeCompetingEdition()
	{
		QuarkusTransaction.requiringNew().run(() -> {
			Edition edition = new Edition();
			edition.date = DATE;
			edition.add(EditionBlock.of("gated", "Gated", "<p>from another instance</p>", false));
			repository.persist(edition);
		});
	}

	private static String html(PrintedEdition edition)
	{
		List<String> parts = edition.blocks().stream().map(PrintedEdition.PrintedBlock::html).toList();
		return String.join("", parts);
	}
}
