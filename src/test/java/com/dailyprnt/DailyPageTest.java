package com.dailyprnt;

import com.dailyprnt.edition.EditionRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class DailyPageTest
{
	@Inject
	EditionRepository repository;

	@AfterEach
	@Transactional
	void cleanup()
	{
		repository.findAll().list().forEach(repository::delete);
	}

	@Test
	void shouldPrintTodaysStrip()
	{
		// when & then
		given()
				.when()
				.get("/daily")
				.then()
				.statusCode(200)
				.contentType(containsString("text/html"))
				.body(containsString("dailyprnt"))
				.body(containsString("class=\"strip\""));
	}

	@Test
	void shouldPrintTheStripForAGivenDate()
	{
		// when & then
		given()
				.when()
				.get("/daily/2026-08-22")
				.then()
				.statusCode(200)
				.body(containsString("Saturday, 22 August 2026"));
	}

	@Test
	void shouldRenderAFailingModuleAsAPlaceholderRatherThanFailingThePage()
	{
		// given
		// the "failing" module always throws for the test profile

		// when & then
		given()
				.when()
				.get("/daily/2026-08-23")
				.then()
				.statusCode(200)
				.body(containsString("Not available today."));
	}

	@Test
	void shouldRejectAMalformedDate()
	{
		// when & then
		given()
				.when()
				.get("/daily/not-a-date")
				.then()
				.statusCode(400);
	}
}
