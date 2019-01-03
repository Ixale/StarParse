package com.ixale.starparse.service.dao;

import java.util.List;

import com.ixale.starparse.domain.Event;

public interface EventDao {

	public void storeEvents(final List<Event> events) throws Exception;

	public void reset() throws Exception;

		/*

----------------------------

WITH RECURSIVE events_fixed (event_id, threat, sub, next_id) AS (

SELECT event_id, threat, threat sub,  next_id
FROM events e 
WHERE e.event_id = 6656

UNION ALL

SELECT e.event_id, e.threat, CASE WHEN (e.threat IS NULL OR e.source_name != 'Ixale') THEN ef.sub WHEN (ef.sub + e.threat) < 0 THEN 0 ELSE (ef.sub + e.threat) END, e.next_id
FROM events_fixed ef
LEFT JOIN events e ON (e.event_id = ef.next_id )
WHERE e.combat_id= 5

)
SELECT * FROM events_fixed
		 */
}
