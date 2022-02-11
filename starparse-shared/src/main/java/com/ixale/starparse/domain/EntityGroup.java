package com.ixale.starparse.domain;

import java.util.ArrayList;
import java.util.HashMap;

public enum EntityGroup {
	EFFECT,

	GUARD,
	DROP,
	MITIGATION,
	ABSORPTION,

	NONREDUCED_THREAT,

	GENERIC,
	;

	private static final HashMap<EntityGroup, ArrayList<Long>> groups = new HashMap<EntityGroup, ArrayList<Long>>();

	public static void addGuid(final EntityGroup group, long guid)
	{
		if (!groups.containsKey(group)) {
			groups.put(group, new ArrayList<Long>());
		}
		groups.get(group).add(guid);
	}

	public static boolean containsGuid(final EntityGroup group, long guid) {
		return groups.containsKey(group) && groups.get(group).contains(guid);
	}
}
