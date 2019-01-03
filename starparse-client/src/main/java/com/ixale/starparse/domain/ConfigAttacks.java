package com.ixale.starparse.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigAttacks implements Serializable {

	private static final long serialVersionUID = 1L;

	public ConfigAttacks() {
	}

	private final List<Long> mr = new ArrayList<>(),
		ft = new ArrayList<>();

	public void setAttacks(final Map<Long, AttackType> attacks) {
		mr.clear();
		ft.clear();
		for (final long guid: attacks.keySet()) {
			switch (attacks.get(guid)) {
			case MR:
				mr.add(guid);
				break;
			case FT:
				ft.add(guid);
				break;
			}
		}
		Collections.sort(mr);
		Collections.sort(ft);
	}

	public Map<Long, AttackType> getAttacks() {
		final Map<Long, AttackType> attacks = new HashMap<Long, AttackType>();
		for (final long guid: mr) {
			attacks.put(guid, AttackType.MR);
		}
		for (final long guid: ft) {
			attacks.put(guid, AttackType.FT);
		}
		return attacks;
	}

	@Override
	public String toString() {
		return "Attacks (" + mr.size() + "-" + ft.size() + ")";
	}
}
