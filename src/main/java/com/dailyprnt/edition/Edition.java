package com.dailyprnt.edition;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The complete strip for one date. Generated once, then replayed unchanged.
 */
@Entity
@Table(name = "edition")
public class Edition extends PanacheEntity
{
	@Column(name = "edition_date", nullable = false, unique = true)
	public LocalDate date;

	@OneToMany(mappedBy = "edition", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("position ASC")
	public List<EditionBlock> blocks = new ArrayList<>();

	public void add(EditionBlock block)
	{
		block.edition = this;
		block.position = blocks.size();
		blocks.add(block);
	}
}
