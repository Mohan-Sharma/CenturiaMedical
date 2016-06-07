package com.nzion.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Table(name="SLOTNOTE")
public class SlotNote extends IdGeneratingBaseEntity {
	
	private static final long serialVersionUID = 1L;
	private Notes notes;

	@OneToOne(targetEntity=Notes.class)
	@JoinColumn(name="NOTES_ID")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
	public Notes getNotes() {
		return notes;
	}

	public void setNotes(Notes notes) {
		this.notes = notes;
	}
}
