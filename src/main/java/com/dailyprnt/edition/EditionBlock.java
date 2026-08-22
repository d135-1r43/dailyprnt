package com.dailyprnt.edition;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One module's contribution to an edition, stored as rendered markup so that a
 * replayed edition never depends on the module or its upstream API again.
 */
@Entity
@Table(name = "edition_block")
public class EditionBlock extends PanacheEntity
{
	@ManyToOne(optional = false)
	@JoinColumn(name = "edition_id", nullable = false)
	public Edition edition;

	@Column(name = "module_id", nullable = false)
	public String moduleId;

	@Column(nullable = false)
	public String title;

	@Column(nullable = false, columnDefinition = "text")
	public String html;

	/** True when the module failed and {@link #html} holds a placeholder. */
	@Column(nullable = false)
	public boolean failed;

	@Column(nullable = false)
	public int position;

	public static EditionBlock of(String moduleId, String title, String html, boolean failed)
	{
		EditionBlock block = new EditionBlock();
		block.moduleId = moduleId;
		block.title = title;
		block.html = html;
		block.failed = failed;
		return block;
	}
}
