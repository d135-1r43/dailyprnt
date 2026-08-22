package com.dailyprnt.edition;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Two requests for the same date arriving together used to race: both found no stored
 * edition, both generated one, and the second insert violated the unique constraint on
 * the date, surfacing as a 500.
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

	@AfterEach
	@Transactional
	void cleanup()
	{
		repository.findAll().list().forEach(repository::delete);
	}

	@Test
	void shouldSurviveTwoRequestsGeneratingTheSameDateAtOnce() throws Exception
	{
		// given both requests are held inside render() until the other arrives, so each is
		// guaranteed to have already missed the lookup for a stored edition
		gatedModule.expectConcurrentRenders(2);
		ExecutorService pool = Executors.newFixedThreadPool(2);

		// when
		Callable<PrintedEdition> request = () -> editions.editionFor(DATE);
		Future<PrintedEdition> first = pool.submit(request);
		Future<PrintedEdition> second = pool.submit(request);
		PrintedEdition a = first.get(30, TimeUnit.SECONDS);
		PrintedEdition b = second.get(30, TimeUnit.SECONDS);
		pool.shutdown();

		// then neither request fails, and both print the one edition that was stored
		assertEquals(1, repository.count(), "the race stored more than one edition for the date");
		assertEquals(DATE, a.date());
		assertEquals(DATE, b.date());
		assertEquals(html(a), html(b), "the two requests printed different strips for the same date");
	}

	@Test
	void shouldStillReplayAStoredEditionAfterARace() throws Exception
	{
		// given a date that was generated under contention
		gatedModule.expectConcurrentRenders(2);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		Callable<PrintedEdition> request = () -> editions.editionFor(DATE);
		Future<PrintedEdition> first = pool.submit(request);
		Future<PrintedEdition> second = pool.submit(request);
		String raced = html(first.get(30, TimeUnit.SECONDS));
		second.get(30, TimeUnit.SECONDS);
		pool.shutdown();

		// when a later request asks for the same date, with the gate no longer held
		gatedModule.expectConcurrentRenders(1);
		PrintedEdition replayed = editions.editionFor(DATE);

		// then it replays the stored edition rather than generating a second one
		assertEquals(1, repository.count());
		assertEquals(raced, html(replayed));
		assertTrue(replayed.blocks().stream().noneMatch(PrintedEdition.PrintedBlock::failed));
	}

	private static String html(PrintedEdition edition)
	{
		List<String> parts = edition.blocks().stream().map(PrintedEdition.PrintedBlock::html).toList();
		return String.join("", parts);
	}
}
