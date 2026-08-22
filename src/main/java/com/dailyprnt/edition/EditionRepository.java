package com.dailyprnt.edition;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.Optional;

@ApplicationScoped
public class EditionRepository implements PanacheRepository<Edition>
{
	public Optional<Edition> findByDate(LocalDate date)
	{
		return find("from Edition e where e.date = ?1", date).firstResultOptional();
	}
}
