package com.ixale.starparse.service.impl;

import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatChallenge;
import com.ixale.starparse.domain.CombatSelection;
import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.EntityGuid;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.stats.AbsorptionStats;
import com.ixale.starparse.domain.stats.ChallengeStats;
import com.ixale.starparse.domain.stats.CombatMitigationStats;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.domain.stats.CombatTickStats;
import com.ixale.starparse.domain.stats.DamageDealtStats;
import com.ixale.starparse.domain.stats.DamageTakenStats;
import com.ixale.starparse.domain.stats.HealingDoneStats;
import com.ixale.starparse.domain.stats.HealingTakenStats;
import com.ixale.starparse.parser.Helpers;
import com.ixale.starparse.service.dao.CombatDao;
import com.ixale.starparse.time.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository("combatDao")
public class CombatDaoImpl extends H2Dao implements CombatDao {

	private static final Logger logger = LoggerFactory.getLogger(CombatDaoImpl.class);

	private static final int LOOKBEHIND_EVENTS = 50, LOOKBEHIND_SECONDS = 5;

	private static final String SQL_INSERT = "INSERT INTO combats"
			+ " (log_id, event_id_from, event_id_to, time_from, time_to"
			+ ", raid_name, boss_name, combat_name"
			+ ", is_pvp"
			+ ", is_running"
			+ ", combat_id)"
			+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",

	SQL_INSERT_STATS = "INSERT INTO combat_stats"
			+ " (event_id_from, event_id_to, time_from, time_to"
			+ ", actions, apm, damage, dps, heal, hps, effective_heal, ehps, ehps_percent"
			+ ", damage_taken, dtps, absorbed, aps"
			+ ", heal_taken, hps_taken, ehps_taken, effective_heal_taken"
			+ ", threat, threat_positive, tps, discipline, combat_id, player_name)"
			+ " VALUES (?, ?, ?, ?, " +
			"?, ?, ?, ?, ?, ?, ?, ?, ?, " +
			"?, ?, ?, ?, " +
			"?, ?, ?, ?, " +
			"?, ?, ?, ?, ?, ?)",

	SQL_UPDATE = "UPDATE combats SET"
			+ " log_id = ?, event_id_from = ?, event_id_to = ?, time_from = ?, time_to = ?"
			+ ", raid_name = ?, boss_name = ?, combat_name = ?"
			+ ", is_pvp = ?"
			+ ", is_running = ?"
			+ " WHERE combat_id = ?",

	SQL_UPDATE_STATS = "UPDATE combat_stats SET"
			+ " event_id_from = ?, event_id_to = ?, time_from = ?, time_to = ?"
			+ ", actions = ?, apm = ?, damage = ?, dps = ?, heal = ?, hps = ?, effective_heal = ?, ehps = ?, ehps_percent = ?"
			+ ", damage_taken = ?, dtps = ?, absorbed = ?, aps = ?"
			+ ", heal_taken = ?, hps_taken = ?, ehps_taken = ?, effective_heal_taken = ?"
			+ ", threat = ?, threat_positive = ?, tps = ?, discipline = ?"
			+ " WHERE combat_id = ? AND player_name = ?",

	SQL_SELECT = "SELECT combat_id, log_id, time_from, time_to, event_id_from, event_id_to"
			+ ", combat_name, raid_name, boss_name, discipline, is_pvp, is_running"
			+ " FROM combats",

	SQL_GET_NAME = "SELECT SUBSTRING(GROUP_CONCAT("
			+ "entity_name || (CASE WHEN entity_count > 1 THEN ' (' || entity_count || ')' ELSE '' END)"
			+ " ORDER BY entity_type = 2 DESC, entity_count DESC, min_event_id"
			+ " SEPARATOR ', '"
			+ "), 0, 255) combat_name"
			+ " FROM ("
			+ "SELECT"
			+ " (CASE WHEN source_type = 1 THEN target_name ELSE source_name END) entity_name"
			+ ", COUNT(DISTINCT (CASE WHEN source_type = 1 THEN target_instance ELSE source_instance END)) entity_count"
			+ ", MIN(CASE WHEN source_type = 1 THEN target_type ELSE source_type END) entity_type"
			+ ", MIN(event_id) min_event_id"
			+ " FROM events e"
			+ " WHERE (target_type != 1 OR source_type != 1)"
			+ " AND (e.event_id BETWEEN ? AND ?)"
			+ " GROUP BY"
			+ " (CASE WHEN source_type = 1 THEN target_name ELSE source_name END)"
			+ ")",

	SQL_GET_STATS_TICKS = "WITH RECURSIVE subs (event_id, timestamp, next_id, duration"
			+ ", damage, damage_sub, heal, heal_sub, effective_heal, effective_heal_sub"
			+ ", damage_taken, damage_taken_sub, absorbed, absorbed_sub"
			+ ", heal_taken, heal_taken_sub, effective_heal_taken, effective_heal_taken_sub, health_sub) AS ("

			+ "SELECT event_id, timestamp, (event_id + 1) AS next_id"
			+ ", 0 AS duration"
			+ ", (CASE WHEN source_name = %playerName AND target_name != %playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END) AS damage"
			+ ", (CASE WHEN source_name = %playerName AND target_name != %playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END) AS damage_sub"
			+ ", (CASE WHEN source_name = %playerName AND effect_guid = " + EntityGuid.Heal + " THEN value ELSE 0 END) AS heal"
			+ ", (CASE WHEN source_name = %playerName AND effect_guid = " + EntityGuid.Heal + " THEN value ELSE 0 END) AS heal_sub"
			+ ", (CASE WHEN source_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND effective_heal IS NOT NULL THEN effective_heal ELSE 0 END) AS effective_heal"
			+ ", (CASE WHEN source_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND effective_heal IS NOT NULL THEN effective_heal ELSE 0 END) AS effective_heal_sub"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END) AS damage_taken"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END) AS damage_taken_sub"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " AND absorbed IS NOT NULL THEN absorbed ELSE 0 END) AS absorbed"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " AND absorbed IS NOT NULL THEN absorbed ELSE 0 END) AS absorbed_sub"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " THEN value ELSE 0 END) AS heal_taken"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " THEN value ELSE 0 END) AS heal_taken_sub"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND effective_heal IS NOT NULL THEN effective_heal ELSE 0 END) AS effective_heal_taken"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND effective_heal IS NOT NULL THEN effective_heal ELSE 0 END) AS effective_heal_taken_sub"
			+ ", (CASE WHEN target_name = %playerName AND source_name != %playerName AND effect_guid = " + EntityGuid.Heal + " AND effective_heal IS NULL THEN 0 ELSE (0"
			+ "+ (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END)"
			+ "+ (CASE WHEN target_name = %playerName AND action_guid = " + EntityGuid.Spend + " AND effect_guid = " + EntityGuid.HealthPoint + " THEN value ELSE 0 END)"
			+ "- (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND effective_heal IS NOT NULL THEN effective_heal ELSE 0 END)"
			+ "- (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " AND absorbed IS NOT NULL THEN absorbed ELSE 0 END)"
			+ "- (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND ability_guid IN (" + EntityGuid.Enure + ", " + EntityGuid.EnduringPain + ") THEN value ELSE 0 END)"
			+ ") END) as health_sub"
			+ " FROM events s "
			+ " WHERE %event_from"
			+ " UNION ALL"
			+ " SELECT e.event_id, e.timestamp, (e.event_id + 1) AS next_id"
			+ ", DATEDIFF(SECOND, %time_from, e.timestamp) AS duration"
			+ ", (CASE WHEN source_name = %playerName AND target_name != %playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END) AS damage"
			+ ", (CASE WHEN source_name = %playerName AND target_name != %playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END) + s.damage_sub AS damage_sub"
			+ ", (CASE WHEN source_name = %playerName AND effect_guid = " + EntityGuid.Heal + " THEN value ELSE 0 END) AS heal"
			+ ", (CASE WHEN source_name = %playerName AND effect_guid = " + EntityGuid.Heal + " THEN value ELSE 0 END) + s.heal_sub AS heal_sub"
			+ ", (CASE WHEN source_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND e.effective_heal  IS NOT NULL THEN e.effective_heal ELSE 0 END) AS effective_heal"
			+ ", (CASE WHEN source_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND e.effective_heal  IS NOT NULL THEN e.effective_heal ELSE 0 END) + s.effective_heal_sub AS effective_heal_sub"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END) AS damage_taken"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END) + s.damage_taken_sub AS damage_taken_sub"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " AND e.absorbed IS NOT NULL THEN e.absorbed ELSE 0 END) AS absorbed"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " AND e.absorbed IS NOT NULL THEN e.absorbed ELSE 0 END) + s.absorbed_sub AS absorbed_sub"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " THEN value ELSE 0 END) AS heal_taken"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " THEN value ELSE 0 END) + s.heal_taken_sub AS heal_taken_sub"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND e.effective_heal IS NOT NULL THEN e.effective_heal ELSE 0 END) AS effective_heal_taken"
			+ ", (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND e.effective_heal IS NOT NULL THEN e.effective_heal ELSE 0 END) + s.effective_heal_taken_sub AS effective_heal_taken_sub"
			+ ", (CASE WHEN target_name = %playerName AND source_name != %playerName AND effect_guid = " + EntityGuid.Heal + " "
			// zero effective heal means we are "probably" full ... or the caster is dead ;-)
			+ "AND e.effective_heal IS NULL THEN 0 ELSE (s.health_sub"
			// non-zero effective heal OR damage, adjust the balance accordingly
			+ "+ (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END)"
			+ "+ (CASE WHEN target_name = %playerName AND action_guid = " + EntityGuid.Spend + " AND effect_guid = " + EntityGuid.HealthPoint + " THEN value ELSE 0 END)"
			+ "- (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND e.effective_heal IS NOT NULL THEN e.effective_heal ELSE 0 END)"
			+ "- (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Damage + " AND e.absorbed IS NOT NULL THEN e.absorbed ELSE 0 END)"
			+ "- (CASE WHEN target_name = %playerName AND effect_guid = " + EntityGuid.Heal + " AND ability_guid IN (" + EntityGuid.Enure + ", " + EntityGuid.EnduringPain + ") THEN e.value ELSE 0 END)"
			+ ") END) as health_sub"
			+ " FROM subs s"
			+ " INNER JOIN events e ON (e.event_id = s.next_id)"
			+ " WHERE %event_to"
			+ ")"
			+ "SELECT CAST(duration AS NUMBER) duration"
			+ ", SUM(CAST (b.damage AS NUMBER)) damage, ROUND(MAX(b.damage_sub) / (duration + 1)) dps"
			+ ", SUM(CAST (b.heal AS NUMBER)) heal , ROUND(MAX(b.heal_sub) / (duration + 1)) hps"
			+ ", SUM(CAST (b.effective_heal AS NUMBER)) effective_heal, ROUND(MAX(b.effective_heal_sub) / (duration + 1)) ehps"
			+ ", CASE WHEN MAX(b.heal_sub) > 0 THEN ROUND(MAX(b.effective_heal_sub) * 100.0 / MAX(b.heal_sub), 2) ELSE null END ehps_percent"
			+ ", SUM(CAST (b.damage_taken AS NUMBER)) damage_taken, ROUND(MAX(b.damage_taken_sub) / (duration + 1)) dtps"
			+ ", SUM(CAST (b.absorbed AS NUMBER)) absorbed, ROUND(MAX(b.absorbed_sub) / (duration + 1)) aps"
			+ ", SUM(CAST (b.heal_taken AS NUMBER)) heal_taken, ROUND(MAX(b.heal_taken_sub) / (duration + 1)) hps_taken"
			+ ", SUM(CAST (b.effective_heal_taken AS NUMBER)) effective_heal_taken, ROUND(MAX(b.effective_heal_taken_sub) / (duration + 1)) ehps_taken"
			+ ", MAX(b.damage_taken_sub) damage_taken_sub, MAX(b.effective_heal_taken_sub) effective_heal_taken_sub, MAX(b.absorbed_sub) absorbed_sub, MAX(b.health_sub) health_sub"
			+ " FROM subs b"
			+ " GROUP BY duration"
			+ " ORDER BY duration",

	SQL_GET_STATS_SUMS_CACHED = "SELECT c.*, DATEDIFF('MILLISECOND', time_from, time_to) duration"
			+ " FROM combat_stats c WHERE combat_id = :combatId AND player_name = :playerName",

	SQL_GET_EVENTS = "SELECT e.*"
			+ ", ae.event_id AS abs_event_id, ae.source_name AS abs_source_name, ae.source_type AS abs_source_type"
			+ ", ae.ability_name AS abs_ability_name, ae.ability_guid AS abs_ability_guid"
			+ " FROM events e"
			+ " LEFT JOIN absorptions a ON (e.absorbed IS NOT NULL AND a.event_id = e.event_id)"
			+ " LEFT JOIN effects af ON (af.effect_id = a.effect_id)"
			+ " LEFT JOIN events ae ON (ae.event_id = af.event_id_from)"
			+ " WHERE e.event_id BETWEEN :eventIdFrom AND :eventIdTo",

	SQL_GET_STATS_SUMS = "SELECT duration, time_to"
			+ ", actions"
			+ ", damage"
			+ ", heal"
			+ ", effective_heal"
			+ ", damage_taken"
			+ ", absorbed"
			+ ", heal_taken"
			+ ", effective_heal_taken"
			+ ", threat, threat_positive, NULL discipline"
			+ " FROM ("
			+ "SELECT LEAST(36000000, GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE MAX(timestamp) END))) AS duration"
			+ ", MAX(timestamp) time_to"
			+ ", SUM(CASE WHEN source_name = :playerName AND effect_guid = " + EntityGuid.AbilityActivate + " THEN 1 ELSE 0 END) AS actions"
			+ ", SUM(CASE WHEN source_name = :playerName AND NOT (target_name = :playerName) AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END) AS damage"
			+ ", SUM(CASE WHEN source_name = :playerName AND effect_guid = " + EntityGuid.Heal + " THEN value ELSE 0 END) AS heal"
			+ ", SUM(CASE WHEN source_name = :playerName AND effect_guid = " + EntityGuid.Heal + " THEN effective_heal ELSE 0 END) AS effective_heal"

			+ ", SUM(CASE WHEN source_name = :playerName THEN effective_threat ELSE 0 END) AS threat"
			+ ", SUM(CASE WHEN source_name = :playerName AND threat > 0 THEN threat ELSE 0 END) AS threat_positive"

			+ ", SUM(CASE WHEN target_name = :playerName AND effect_guid = " + EntityGuid.Damage + " THEN value ELSE 0 END) AS damage_taken"
			+ ", SUM(CASE WHEN target_name = :playerName AND effect_guid = " + EntityGuid.Damage + " AND absorbed IS NOT NULL THEN absorbed ELSE 0 END) AS absorbed"
			+ ", SUM(CASE WHEN target_name = :playerName AND effect_guid = " + EntityGuid.Heal + " THEN value ELSE 0 END) AS heal_taken"
			+ ", SUM(CASE WHEN target_name = :playerName AND effect_guid = " + EntityGuid.Heal + " THEN effective_heal ELSE 0 END) AS effective_heal_taken"
			+ " FROM EVENTS e"
			+ " WHERE (e.event_id BETWEEN :eventIdFrom AND :eventIdTo) AND (:playerName IN (source_name, target_name) OR event_id = :eventIdTo)"
			+ ")",

	SQL_GET_MITIGATION_STATS_SUMS = "SELECT duration, ticks, damage"
			+ ", internal, CASE WHEN internal > 0 THEN ROUND(internal * 100.0 / damage, 1) ELSE 0 END AS pct_internal"
			+ ", elemental, CASE WHEN elemental > 0 THEN ROUND(elemental * 100.0 / damage, 1) ELSE 0 END AS pct_elemental"
			+ ", energy, CASE WHEN energy > 0 THEN ROUND(energy * 100.0 / damage, 1) ELSE 0 END AS pct_energy"
			+ ", kinetic, CASE WHEN kinetic > 0 THEN ROUND(kinetic * 100.0 / damage, 1) ELSE 0 END AS pct_kinetic"

			+ ", ticks_miss, CASE WHEN ticks_miss > 0 THEN ROUND(ticks_miss * 100.0 / ticks, 1) ELSE 0 END AS pct_miss"
			+ ", ticks_shield, CASE WHEN ticks_shield > 0 THEN ROUND(ticks_shield * 100.0 / ticks, 1) ELSE 0 END AS pct_shield"

			+ ", absorbed, CASE WHEN absorbed > 0 THEN ROUND(absorbed * 100.0 / damage, 1) ELSE 0 END AS pct_absorbed"
			+ ", absorbed_self, CASE WHEN absorbed_self > 0 THEN ROUND(absorbed_self * 100.0 / damage, 1) ELSE 0 END AS pct_absorbed_self"
			+ ", ROUND(absorbed * 1000.0 / duration) AS aps"
			+ " FROM ("
			+ "SELECT GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE MAX(e.timestamp) END)) AS duration"
			+ ", SUM(CASE WHEN e.damage_guid = " + EntityGuid.Internal + " THEN e.value ELSE 0 END) AS internal"
			+ ", SUM(CASE WHEN e.damage_guid = " + EntityGuid.Elemental + " THEN e.value ELSE 0 END) AS elemental"
			+ ", SUM(CASE WHEN e.damage_guid = " + EntityGuid.Energy + " THEN e.value ELSE 0 END) AS energy"
			+ ", SUM(CASE WHEN e.damage_guid = " + EntityGuid.Kinetic + " THEN e.value ELSE 0 END) AS kinetic"

			+ ", SUM(CASE WHEN e.mitigation_name IS NOT NULL AND e.mitigation_guid != " + EntityGuid.Immune + " AND e.value > 0 THEN 1 ELSE 0 END) AS ticks_shield"
			+ ", SUM(CASE WHEN e.mitigation_name IS NOT NULL AND e.mitigation_guid != " + EntityGuid.Immune + " AND e.value = 0 THEN 1 ELSE 0 END) AS ticks_miss"
			+ ", SUM(CASE WHEN e.mitigation_name IS NULL OR e.mitigation_guid != " + EntityGuid.Immune + " THEN 1 ELSE 0 END) AS ticks"

			+ ", SUM(CASE WHEN e.absorbed IS NOT NULL THEN e.absorbed ELSE 0 END) AS absorbed"
			+ ", SUM(CASE WHEN e.absorbed IS NOT NULL AND (ae.source_name = :playerName OR ae.source_type IS NULL) THEN e.absorbed ELSE 0 END) AS absorbed_self"

			+ ", SUM(e.value) damage"

			+ " FROM events e"
			+ " LEFT JOIN absorptions a ON (a.event_id = e.event_id)"
			+ " LEFT JOIN effects af ON (af.effect_id = a.effect_id)"
			+ " LEFT JOIN events ae ON (ae.event_id = af.event_id_from)"
			+ " WHERE (e.event_id BETWEEN :eventIdFrom AND :eventIdTo)"
			+ " AND e.target_name = :playerName AND e.effect_guid = " + EntityGuid.Damage + ""
			+ ")",

	SQL_GET_DOT_NAMES = "SELECT e.effect_guid, e.ability_name, a.actions, a.ability_guid FROM ("
			+ " SELECT e.effect_guid, e.ability_name, e.ability_guid"
			+ " FROM effects f "
			+ " INNER JOIN events e ON (f.event_id_from = e.event_id)"
			+ " WHERE f.event_id_to > :eventIdFrom AND f.event_id_from < :eventIdTo"
			+ " AND source_name = :playerName"
			+ " GROUP BY e.effect_guid"
			+ " ) e"
			+ " INNER JOIN ("
			+ " SELECT ability_guid, COUNT(*) actions"
			+ " FROM events"
			+ " WHERE (event_id BETWEEN :behindEventIdFrom AND :eventIdTo AND timestamp >= :behindTimeFrom)"
			+ " AND source_name = :playerName AND effect_guid = " + EntityGuid.AbilityActivate
			+ " GROUP BY ability_guid"
			+ " ) a ON (a.ability_guid = e.ability_guid)",

	SQL_GET_TARGET_NAMES = "SELECT target_type, target_name, target_guid, target_instance"
			+ ", MIN(timestamp) AS target_time_from, MAX(timestamp) AS target_time_to"
			+ " FROM events e "
			+ " WHERE event_id BETWEEN :eventIdFrom AND :eventIdTo"
			+ " GROUP BY target_type, target_name, target_guid, target_instance",

	SQL_GET_DAMATE_DEALT_SUMS =
			"SUM(CASE WHEN effect_guid = " + EntityGuid.AbilityActivate + " THEN 1 ELSE 0 END) actions"
					+ ", SUM(CASE WHEN effect_guid = " + EntityGuid.Damage + " THEN 1 ELSE 0 END) ticks"
					+ ", SUM(CASE WHEN NOT is_crit THEN 1 ELSE 0 END) ticks_normal"
					+ ", SUM(CASE WHEN is_crit THEN 1 ELSE 0 END) ticks_crit"
					+ ", SUM(CASE WHEN mitigation_name IS NOT NULL AND mitigation_guid != " + EntityGuid.Immune + " THEN 1 ELSE 0 END) ticks_miss"
					+ ", SUM(CASE WHEN mitigation_name IS NOT NULL AND mitigation_guid = " + EntityGuid.Immune + " THEN 1 ELSE 0 END) ticks_immune"

					+ ", SUM(CASE WHEN NOT is_crit THEN value ELSE 0 END) total_normal"
					+ ", SUM(CASE WHEN is_crit THEN value ELSE 0 END) total_crit"
					+ ", MAX(value) max, SUM(value) total"
					+ ", GROUP_CONCAT(DISTINCT damage_name) damage_type"
					+ ", MIN(timestamp) sub_time_from, MAX(timestamp) sub_time_to"
					+ " FROM events e"
					+ " WHERE (event_id BETWEEN :behindEventIdFrom AND :eventIdTo AND timestamp >= :behindTimeFrom)" // catch casts before combat start
					+ " AND source_name = :playerName"
					+ " AND ((effect_guid = " + EntityGuid.Damage + " AND target_name != :playerName) OR (effect_guid = " + EntityGuid.AbilityActivate + "))"
					+ " AND ability_guid IN (SELECT ability_guid FROM events WHERE effect_guid = " + EntityGuid.Damage + " AND source_name = :playerName AND event_id BETWEEN :eventIdFrom AND :eventIdTo)",

	SQL_GET_DAMAGE_DEALT_TOTALS = "SELECT a.*"
			+ ", (CASE WHEN (ticks_normal - ticks_miss - ticks_immune) > 0 THEN ROUND(a.total_normal * 1.0 / (ticks_normal - ticks_miss - ticks_immune)) ELSE 0 END) avg_normal"
			+ ", (CASE WHEN (ticks - ticks_miss - ticks_immune) > 0 THEN ROUND(a.total * 1.0 / (ticks - ticks_miss - ticks_immune)) ELSE 0 END) avg_hit"
			+ ", (CASE WHEN ticks_crit > 0 THEN ROUND(total_crit * 1.0 / ticks_crit) ELSE 0 END) avg_crit"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(ticks_crit * 100.0 / ticks, 2) ELSE 0 END) AS pct_crit"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(ticks_miss * 100.0 / ticks, 2) ELSE 0 END) AS pct_miss"
			+ ", (CASE WHEN t.total > 0 THEN ROUND(a.total * 100.0 / t.total, 1) ELSE 0 END) AS pct_total"
			+ ", ROUND(a.total * 1000.0 / GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE t.timestamp END))) dps"
			+ ", dots.ability_name AS dot_name, dots.actions AS dot_actions, dots.ability_guid AS dot_guid"
			+ ", tt.target_time_from, tt.target_time_to, a.sub_time_from, a.sub_time_to"
			+ " FROM ("
			+ " SELECT %pivots_cols, " // ability_name, ability_guid
			+ SQL_GET_DAMATE_DEALT_SUMS
			+ " %pivots_where" // target_name != :playerName
			+ " GROUP BY %pivots_group" // ability_name, ability_guid
			+ " ) a"
			+ " INNER JOIN ("
			+ " SELECT SUM(CASE WHEN effect_guid = " + EntityGuid.Damage + " AND source_name = :playerName AND target_name != :playerName THEN value ELSE 0 END) total, MAX(timestamp) timestamp"
			+ " FROM events e"
			+ " WHERE e.event_id BETWEEN :eventIdFrom AND :eventIdTo"
			+ ") t"
			// dot names and action counts
			+ " LEFT JOIN ("
			+ SQL_GET_DOT_NAMES
			+ ") dots ON (dots.effect_guid = a.ability_guid AND dots.ability_name != a.ability_name)"
			// targets
			+ "LEFT JOIN ("
			+ SQL_GET_TARGET_NAMES
			+ " ) tt ON (tt.target_name = a.target_name AND tt.target_instance = a.target_instance)"
			+ " ORDER BY a.total DESC",

	SQL_GET_DAMAGE_DEALT_SIMPLE = "SELECT a.*"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(ticks_crit * 100.0 / ticks, 2) ELSE 0 END) AS pct_crit"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(ticks_miss * 100.0 / ticks, 2) ELSE 0 END) AS pct_miss"
			+ ", ROUND(a.total * 1000.0 / GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE t.timestamp END))) dps"
			+ " FROM ("
			+ " SELECT " // ability_name, ability_guid
			+ SQL_GET_DAMATE_DEALT_SUMS
			+ " AND e.target_name != :playerName" // target_name != :playerName
			+ " ) a"
			+ " INNER JOIN ("
			+ " SELECT MAX(timestamp) timestamp"
			+ " FROM events e"
			+ " WHERE e.event_id BETWEEN :eventIdFrom AND :eventIdTo"
			+ ") t",

	SQL_GET_HEALING_DONE_SUMS =
			", SUM(CASE WHEN effect_guid = " + EntityGuid.AbilityActivate + " THEN 1 ELSE 0 END) actions"
					+ ", SUM(CASE WHEN effect_guid = " + EntityGuid.Heal + " OR absorbed > 0 THEN 1 ELSE 0 END) ticks"
					+ ", SUM(ticks_normal) ticks_normal"
					+ ", SUM(ticks_crit) ticks_crit"
					+ ", SUM(total_normal) total_normal"
					+ ", SUM(total_crit) total_crit"
					+ ", SUM(total_effective) total_effective"
					+ ", GREATEST(MAX(total), MAX(absorbed)) max"
					+ ", SUM(total) total"
					+ ", SUM(absorbed) absorbed"
					+ ", MIN(timestamp) sub_time_from, MAX(timestamp) sub_time_to"
					+ " FROM ("
					+ "SELECT e.target_name, e.target_instance, e.ability_name, e.ability_guid, e.effect_guid"
					+ ", e.event_id"
					+ ", (CASE WHEN NOT is_crit THEN 1 ELSE 0 END) ticks_normal"
					+ ", (CASE WHEN is_crit THEN 1 ELSE 0 END) ticks_crit"
					+ ", (CASE WHEN NOT is_crit THEN value ELSE 0 END) total_normal"
					+ ", (CASE WHEN is_crit THEN value ELSE 0 END) total_crit"
					+ ", (CASE WHEN effective_heal IS NOT NULL THEN effective_heal ELSE 0 END) total_effective"
					+ ", (value) total"
					+ ", 0 absorbed"
					+ ", e.timestamp"
					+ " FROM events e"
					+ " WHERE event_id BETWEEN :eventIdFrom AND :eventIdTo"
					+ " AND source_name = :playerName"
					+ " AND effect_guid IN (" + EntityGuid.Heal + ", " + EntityGuid.AbilityActivate + ")"
					+ " AND ability_guid IN (SELECT ability_guid FROM events WHERE effect_guid = " + EntityGuid.Heal + " AND source_name = :playerName AND event_id BETWEEN :eventIdFrom AND :eventIdTo)"
					+ " UNION ALL"
					+ " SELECT e.target_name, e.target_instance, e.ability_name, e.ability_guid, e.effect_guid"
					+ ", e.event_id"
					+ ", 0 ticks_normal"
					+ ", 0 ticks_crit"
					+ ", 0 total_normal"
					+ ", 0 total_crit"
					+ ", 0 total_effective"
					+ ", 0 total"
					+ ", ea.absorbed"
					+ ", e.timestamp"
					+ " FROM events e"
					+ " INNER JOIN effects f ON (f.is_absorption = TRUE AND f.event_id_from = e.event_id)"
					+ " INNER JOIN absorptions a ON (a.effect_id = f.effect_id)"
					+ " INNER JOIN events ea ON (ea.event_id = a.event_id)"
					+ " WHERE ea.event_id BETWEEN :eventIdFrom AND :eventIdTo"
					+ " AND e.source_name = :playerName) e",

	SQL_GET_HEALING_DONE_TOTALS = "SELECT a.*"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(a.total * 1.0 / ticks) ELSE 0 END) avg_normal"
			+ ", (CASE WHEN ticks_crit > 0 THEN ROUND(total_crit * 1.0 / ticks_crit) ELSE 0 END) avg_crit"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(ticks_crit * 100.0 / ticks, 2) ELSE 0 END) AS pct_crit"
			+ ", (CASE WHEN a.total > 0 THEN ROUND(a.total_effective * 100.0 / a.total, 1) ELSE 0 END) AS pct_effective"
			+ ", (CASE WHEN t.total_effective + t.total_absorbed > 0 THEN ROUND((a.total_effective + a.absorbed) * 100.0 / (t.total_effective + t.total_absorbed), 1) ELSE 0 END) AS pct_total"
			+ ", ROUND(a.total * 1000.0 / GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE t.timestamp END))) hps"
			+ ", ROUND(a.total_effective * 1000.0 / GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE t.timestamp END))) ehps"
			+ ", ROUND(a.absorbed * 1000.0 / GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE t.timestamp END))) aps"
			+ ", dots.ability_name AS dot_name, dots.actions AS dot_actions"
			+ ", tt.target_time_from, tt.target_time_to, a.sub_time_from, a.sub_time_to"
			+ " FROM ("
			+ " SELECT %pivots_cols" // ability_name, ability_guid
			+ SQL_GET_HEALING_DONE_SUMS
			+ " %pivots_where" // target_name != :playerName
			+ " GROUP BY %pivots_group" // ability_name, ability_guid
			+ " ) a"
			+ " INNER JOIN ("
			+ " SELECT SUM(CASE WHEN effect_guid = " + EntityGuid.Heal + " AND source_name = :playerName THEN value ELSE 0 END) total"
			+ ", SUM(CASE WHEN effective_heal IS NOT NULL AND source_name = :playerName THEN effective_heal ELSE 0 END) total_effective"
			//+ ", SUM(CASE WHEN absorbed IS NOT NULL AND target_name = :playerName THEN absorbed ELSE 0 END) total_absorbed" // XXX
			+ ", (SELECT SUM(xea.absorbed) FROM events xe"
			+ "		INNER JOIN effects xf ON (xf.is_absorption = TRUE AND xf.event_id_from = xe.event_id)"
			+ " 	INNER JOIN absorptions xa ON (xa.effect_id = xf.effect_id)"
			+ " 	INNER JOIN events xea ON (xea.event_id = xa.event_id)"
			+ " 	WHERE xea.event_id BETWEEN :eventIdFrom AND :eventIdTo"
			+ " 		AND xe.source_name = :playerName) total_absorbed"
			+ ", MAX(timestamp) timestamp"
			+ " FROM events e"
			+ " WHERE e.event_id BETWEEN :eventIdFrom AND :eventIdTo"
			+ ") t"
			// dot names and action counts
			+ " LEFT JOIN ("
			+ SQL_GET_DOT_NAMES
			+ ") dots ON (dots.effect_guid = a.ability_guid AND dots.ability_name != a.ability_name)"
			// targets
			+ " LEFT JOIN ("
			+ SQL_GET_TARGET_NAMES
			+ " ) tt ON (tt.target_name = a.target_name AND tt.target_instance = a.target_instance)"
			+ " ORDER BY a.total DESC",

	SQL_GET_SOURCE_NAMES = "SELECT source_type, source_name, source_guid, source_instance"
			+ ", MIN(timestamp) AS source_time_from, MAX(timestamp) AS source_time_to"
			+ " FROM events e "
			+ " WHERE event_id BETWEEN :eventIdFrom AND :eventIdTo"
			+ " GROUP BY source_type, source_name, source_guid, source_instance",

	SQL_GET_DAMAGE_TAKEN_SUMS =
			", COUNT(*) ticks"
					+ ", SUM(CASE WHEN mitigation_name IS NOT NULL AND mitigation_guid != " + EntityGuid.Immune + " AND value > 0 THEN 1 ELSE 0 END) ticks_shield"
					+ ", SUM(CASE WHEN mitigation_name IS NOT NULL AND mitigation_guid != " + EntityGuid.Immune + " AND value = 0 THEN 1 ELSE 0 END) ticks_miss"
					+ ", SUM(absorbed) total_absorbed"
					+ ", SUM(value) total"
					+ ", SUM(CASE WHEN (damage_guid IN (" + EntityGuid.Internal + ", " + EntityGuid.Elemental + ")) THEN value ELSE 0 END) total_ie"
					+ ", MAX(value) max"
					+ ", GROUP_CONCAT(DISTINCT damage_name) damage_type"
					+ ", MIN(timestamp) sub_time_from, MAX(timestamp) sub_time_to"
					+ " FROM events e"
					+ " WHERE event_id BETWEEN :eventIdFrom AND :eventIdTo"
					+ " AND target_name = :playerName"
					+ " AND effect_guid IN (" + EntityGuid.Damage + ")",

	SQL_GET_DAMAGE_TAKEN_TOTALS = "SELECT a.*"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(a.total * 1.0 / ticks) ELSE 0 END) avg_normal"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(ticks_shield * 100.0 / ticks, 2) ELSE 0 END) AS pct_shield"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(ticks_miss * 100.0 / ticks, 2) ELSE 0 END) AS pct_miss"
			+ ", (CASE WHEN t.total > 0 THEN ROUND(a.total * 100.0 / t.total, 1) ELSE 0 END) AS pct_total"
			+ ", ROUND(a.total * 1000.0 / GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE t.timestamp END))) dtps"
			+ ", ts.source_time_from, ts.source_time_to, a.sub_time_from, a.sub_time_to"
			// sums
			+ " FROM ("
			+ " SELECT %pivots_cols" // ability_name, ability_guid
			+ SQL_GET_DAMAGE_TAKEN_SUMS
			+ " %pivots_where" // target_name != :playerName
			+ " GROUP BY %pivots_group" // ability_name, ability_guid
			+ " ) a"
			// total
			+ " INNER JOIN ("
			+ " SELECT SUM(CASE WHEN effect_guid = " + EntityGuid.Damage + " AND target_name = :playerName THEN value ELSE 0 END) total, MAX(timestamp) timestamp"
			+ " FROM events e"
			+ " WHERE e.event_id BETWEEN :eventIdFrom AND :eventIdTo"
			+ ") t"
			// sources
			+ " LEFT JOIN ("
			+ SQL_GET_SOURCE_NAMES
			+ " ) ts ON (ts.source_name = a.source_name AND ts.source_instance = a.source_instance)"
			+ " ORDER BY a.total DESC",

	SQL_GET_HEALING_TAKEN_SUMS =
			", COUNT(DISTINCT event_id) ticks"
					+ ", SUM(ticks_normal) ticks_normal"
					+ ", SUM(ticks_crit) ticks_crit"
					+ ", SUM(total_normal) total_normal"
					+ ", SUM(total_crit) total_crit"
					+ ", SUM(total_effective) total_effective"
					+ ", SUM(e.total) total"
					+ ", SUM(absorbed) absorbed"
					+ ", MIN(timestamp) sub_time_from, MAX(timestamp) sub_time_to"
					+ " FROM ("
					+ "SELECT e.source_name, e.source_instance, e.ability_name, e.ability_guid"
					+ ", e.event_id"
					+ ", (CASE WHEN NOT is_crit THEN 1 ELSE 0 END) ticks_normal"
					+ ", (CASE WHEN is_crit THEN 1 ELSE 0 END) ticks_crit"

					+ ", (CASE WHEN NOT is_crit THEN value ELSE 0 END) total_normal"
					+ ", (CASE WHEN is_crit THEN value ELSE 0 END) total_crit"
					+ ", (CASE WHEN effective_heal IS NOT NULL THEN effective_heal ELSE 0 END) total_effective"
					+ ", (value) total"
					+ ", 0 absorbed"
					+ ", timestamp"
					+ " FROM events e"
					+ " WHERE event_id BETWEEN :eventIdFrom AND :eventIdTo"
					+ " AND target_name = :playerName"
					+ " AND effect_guid IN (" + EntityGuid.Heal + ")"
					+ " UNION ALL"
					+ " SELECT e.source_name, e.source_instance, e.ability_name, e.ability_guid"
					+ ", e.event_id"
					+ ", 0 ticks_normal"
					+ ", 0 ticks_crit"

					+ ", 0 total_normal"
					+ ", 0 total_crit"
					+ ", 0 total_effective"
					+ ", 0 total"
					+ ", ea.absorbed"
					+ ", e.timestamp"
					+ " FROM events e"
					+ " INNER JOIN effects f ON (f.is_absorption = TRUE AND f.event_id_from = e.event_id)"
					+ " INNER JOIN absorptions a ON (a.effect_id = f.effect_id)"
					+ " INNER JOIN events ea ON (ea.event_id = a.event_id)"
					+ " WHERE ea.event_id BETWEEN :eventIdFrom AND :eventIdTo"
					+ " AND e.target_name = :playerName) e",

	SQL_GET_HEALING_TAKEN_TOTALS = "SELECT a.*"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(a.total * 1.0 / ticks) ELSE 0 END) avg_normal"
			+ ", (CASE WHEN ticks_crit > 0 THEN ROUND(total_crit * 1.0 / ticks_crit) ELSE 0 END) avg_crit"
			+ ", (CASE WHEN ticks > 0 THEN ROUND(ticks_crit * 100.0 / ticks, 2) ELSE 0 END) AS pct_crit"
			+ ", (CASE WHEN a.total > 0 THEN ROUND(a.total_effective * 100.0 / a.total, 1) ELSE 0 END) AS pct_effective"
			+ ", (CASE WHEN t.total_effective + t.total_absorbed > 0 THEN ROUND((a.total_effective + a.absorbed) * 100.0 / (t.total_effective + t.total_absorbed), 1) ELSE 0 END) AS pct_total"
			+ ", ROUND(a.total * 1000.0 / GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE t.timestamp END))) htps"
			+ ", ROUND(a.total_effective * 1000.0 / GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE t.timestamp END))) ehtps"
			+ ", ROUND(a.absorbed * 1000.0 / GREATEST(1000, DATEDIFF('MILLISECOND', :timeFrom, CASE WHEN :timeTo IS NOT NULL THEN :timeTo ELSE t.timestamp END))) aps"
			+ ", ts.source_time_from, ts.source_time_to, a.sub_time_from, a.sub_time_to"
			// sums
			+ " FROM ("
			+ " SELECT %pivots_cols" // ability_name, ability_guid
			+ SQL_GET_HEALING_TAKEN_SUMS
			+ " %pivots_where" // target_name != :playerName
			+ " GROUP BY %pivots_group" // ability_name, ability_guid
			+ " ) a"
			// total
			+ " INNER JOIN ("
			+ " SELECT SUM(CASE WHEN effect_guid = " + EntityGuid.Heal + " AND target_name = :playerName THEN value ELSE 0 END) total"
			+ ", SUM(CASE WHEN effective_heal IS NOT NULL AND target_name = :playerName THEN effective_heal ELSE 0 END) total_effective"
			+ ", SUM(CASE WHEN absorbed IS NOT NULL AND target_name = :playerName THEN absorbed ELSE 0 END) total_absorbed"
			+ ", MAX(timestamp) timestamp"
			+ " FROM events e"
			+ " WHERE e.event_id BETWEEN :eventIdFrom AND :eventIdTo"
			+ ") t"
			// sources
			+ " LEFT JOIN ("
			+ SQL_GET_SOURCE_NAMES
			+ " ) ts ON (ts.source_name = a.source_name AND ts.source_instance = a.source_instance)"
			+ " ORDER BY a.total DESC",

	SQL_GET_COMBAT_EFFECTS =
			// buffs
			"SELECT f.effect_id, f.event_id_from, f.event_id_to, f.time_from, f.time_to"
					+ ", e.source_name, e.source_type, e.target_name, e.target_type"
					+ ", e.ability_name, e.ability_guid, e.effect_name, e.effect_guid"
					+ ", f.is_activated, f.is_absorption"
					+ " FROM effects f"
					+ " INNER JOIN events e ON (e.event_id = f.event_id_from)"
					+ " WHERE (f.event_id_to >= :eventIdFrom OR (f.event_id_to IS NULL AND f.event_id_from >= :eventIdFrom)) AND f.event_id_from <= :eventIdTo"
					+ " ORDER BY f.effect_id",

	SQL_GET_ABSORPTION_TAKEN_TOTALS =
			"SELECT e.source_name, e.effect_guid, e.effect_name, SUM(ea.absorbed) total"
					+ " FROM events ea"
					+ " INNER JOIN absorptions a ON (ea.event_id = a.event_id)"
					+ " INNER JOIN effects ef ON (ef.effect_id = a.effect_id)"
					+ " INNER JOIN events e ON (e.event_id = ef.event_id_from)"
					+ " WHERE ea.target_name = :playerName "
					+ " AND ea.event_id BETWEEN :eventIdFrom AND :eventIdTo"
					+ " GROUP BY e.source_name, e.effect_guid, e.effect_name",

	SQL_GET_COMBAT_CHALLENGES =
			"SELECT phase_id, name, type, combat_id, event_id_from, event_id_to, tick_from, tick_to"
					+ " FROM phases"
					+ " WHERE combat_id = :combatId"
					+ " AND (tick_to IS NULL OR tick_to >= :tickFrom) AND tick_from <= :tickTo"
					+ " AND name IN (:phaseNames)"
					+ " ORDER BY event_id_from ASC";

	private Context context;

	private List<CombatChallenge> availableChallenges;
	private final HashMap<String, CombatChallenge> phasesToChallenges = new HashMap<>();
	private final HashMap<Integer, HashMap<String, HashMap<Long, ChallengeStats>>> cachedChallenges = new HashMap<>();

	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	final Calendar cal = Calendar.getInstance(TimeUtils.getCurrentTimezone());

	static class Boundaries {
		Integer eventIdFrom, eventIdTo;
		Timestamp timeFrom, timeTo;
	}

	@Autowired
	public void setContext(Context context) {
		this.context = context;
	}

	private Integer getContextEventIdFrom(final Combat combat, final CombatSelection combatSel) throws Exception {
		try {
			if (combatSel != null && combatSel.getTickFrom() != null) {
				return getJdbcTemplate().queryForObject("SELECT event_id FROM events"
						+ " WHERE event_id >= " + (combatSel.getEventIdFrom() != null ? combatSel.getEventIdFrom() : combat.getEventIdFrom())
						+ " AND timestamp >= '" + sdf.format(combat.getTimeFrom() + combatSel.getTickFrom()) + "'"
						+ " ORDER BY event_id LIMIT 1", Integer.class);
			}
		} catch (DataAccessException e) {
			logger.warn("Error while resolving start of window for [" + combat + "] @ " + combatSel.getTickFrom(), e);
		}
		// combat start by default
		return (combatSel != null && combatSel.getEventIdFrom() != null ? combatSel.getEventIdFrom() : combat.getEventIdFrom());
	}

	private Integer getContextEventIdTo(final Combat combat, final CombatSelection combatSel) throws Exception {
		try {
			if (combatSel != null && combatSel.getTickTo() != null) {
				return getJdbcTemplate().queryForObject("SELECT event_id FROM events"
						+ " WHERE " + (combatSel.getEventIdTo() != null
						? "event_id <= " + combatSel.getEventIdTo()
						: (combat.getEventIdTo() != null ? "event_id <= " + combat.getEventIdTo() : "event_id >= " + combat.getEventIdFrom()))
						+ " AND timestamp <= '" + sdf.format(combat.getTimeFrom() + combatSel.getTickTo()) + "'"
						+ " ORDER BY event_id DESC LIMIT 1", Integer.class);
			}
		} catch (DataAccessException e) {
			logger.warn("Error while resolving end of window for [" + combat + "] @ " + combatSel.getTickTo(), e);
		}
		// combat end by default (where available)
		return ((combatSel != null && combatSel.getEventIdTo() != null)
				? combatSel.getEventIdTo()
				: (combat.getEventIdTo() != null ? combat.getEventIdTo() : Integer.MAX_VALUE));
	}

	private Boundaries getBoundaries(final Combat combat, final CombatSelection combatSel) throws Exception {

		final Boundaries bounds = new Boundaries();

		bounds.eventIdFrom = getContextEventIdFrom(combat, combatSel);
		bounds.eventIdTo = getContextEventIdTo(combat, combatSel);

		bounds.timeFrom = new Timestamp(combat.getTimeFrom() + ((combatSel != null && combatSel.getTickFrom() != null) ? combatSel.getTickFrom() : 0));
		bounds.timeTo = (combatSel != null && combatSel.getTickTo() != null)
				? new Timestamp(combat.getTimeFrom() + combatSel.getTickTo())
				: (combat.getTimeTo() != null ? new Timestamp(combat.getTimeTo()) : null);

		return bounds;
	}

	@Override
	public void storeCombats(final List<Combat> combats, final Combat currentCombat) throws Exception {

		// any unfinished combats?
		ArrayList<Integer> storedCombatIds = new ArrayList<>();
		Integer runningCombatId;
		runningCombatId = getJdbcTemplate().query("SELECT combat_id FROM combats WHERE is_running = TRUE",
				rs -> rs.next() ? rs.getInt("combat_id") : null);

		// update or close
		if (runningCombatId != null) {
			if (currentCombat != null && runningCombatId.equals(currentCombat.getCombatId())) {
				// prolong
				storeCombat(currentCombat, false, false);
			} else if (combats.size() > 0 && runningCombatId.equals(combats.get(0).getCombatId())) {
				// close
				storeCombat(combats.get(0), false, true);
			} else {
				throw new Exception("Unknown running combat [" + runningCombatId + "]");
			}
			// skip the just processed ID
			storedCombatIds.add(runningCombatId);
		}

		// process already finished
		for (Combat c : combats) {
			if (storedCombatIds.contains(c.getCombatId())) {
				continue;
			}
			storeCombat(c, true, true);
		}

		// process current
		if (currentCombat != null && !storedCombatIds.contains(currentCombat.getCombatId())) {
			storeCombat(currentCombat, true, false);
		}
	}

	private void storeCombat(final Combat c, boolean create, boolean close) throws Exception {
		// resolve name
		final String combatName = close ? getJdbcTemplate().query(SQL_GET_NAME, new Object[]{
				c.getEventIdFrom(),
				c.getEventIdTo() != null ? c.getEventIdTo() : Integer.MAX_VALUE
		}, rs -> rs.next() ? rs.getString("combat_name") : null) : null /* currently running */;

		final Timestamp fromTs = new Timestamp(c.getTimeFrom());

		Integer selfTick = null;
		for (final Map.Entry<Actor, CharacterDiscipline> entry : context.getCombatInfo().get(c.getCombatId()).getCombatPlayers().entrySet()) {
			// gather statistics
			final CombatStats stats = getCombatStats(c, new CombatSelection(c.getEventIdFrom(), c.getEventIdTo(), null, null), entry.getKey().getName());

			// insert or update (same structure)
			final Timestamp toTs = new Timestamp(c.getTimeTo() != null ? c.getTimeTo() : c.getTimeFrom() + stats.getTick());
			if (selfTick == null) {
				selfTick = stats.getTick(); // take first encountered (== this player)
			}

			getJdbcTemplate().update(create ? SQL_INSERT_STATS : SQL_UPDATE_STATS, new Object[]{
					c.getEventIdFrom(),
					c.getEventIdTo(),
					fromTs,
					toTs,
					stats.getActions(),
					stats.getApm(), stats.getDamage(), stats.getDps(), stats.getHeal(), stats.getHps(), stats.getEffectiveHeal(), stats.getEhps(), stats.getEhpsPercent(),
					stats.getDamageTaken(), stats.getDtps(),
					stats.getAbsorbed(), stats.getAps(),
					stats.getHealTaken(), stats.getHpsTaken(), stats.getEhpsTaken(), stats.getEffectiveHealTakenTotal(),
					stats.getThreat(), stats.getThreatPositive(), stats.getTps(),
					entry.getValue() != null ? entry.getValue().name() : null,
					c.getCombatId(),
					entry.getKey().getName()
			});
		}

		getJdbcTemplate().update(create ? SQL_INSERT : SQL_UPDATE, new Object[]{
				c.getLogId(),
				c.getEventIdFrom(),
				c.getEventIdTo(),
				fromTs,
				new Timestamp(c.getTimeTo() != null ? c.getTimeTo() : c.getTimeFrom() + (selfTick == null ? 1000 : selfTick)),
				c.getBoss() != null ? c.getBoss().getRaid().getName() : null,
				c.getBoss() != null ? c.getBoss().toString() : null,
				combatName,
				c.isPvp() != null ? c.isPvp() : null,
				!close,
				c.getCombatId()
		});
	}

	@Override
	public Combat findCombat(int combatId) throws Exception {
		return getCombat(combatId);
	}

	private final RowMapper<Combat> COMBAT_ROW_MAPPER = (rs, rowNum) -> {

		final Combat c = new Combat(
				rs.getInt("combat_id"),
				rs.getInt("log_id"),
				getTimestamp(rs, "time_from").getTime(),
				rs.getInt("event_id_from"));

		c.setName(rs.getString("combat_name"));
		c.setIsRunning(rs.getBoolean("is_running"));

		if (rs.getString("boss_name") != null) {
			c.setBoss(Helpers.getRaidBossByVerbose(rs.getString("boss_name")));
		}
		c.setIsPvp(rs.getBoolean("is_pvp"));

		if (rs.getInt("event_id_to") > 0) {
			c.setEventIdTo(rs.getInt("event_id_to"));
			c.setTimeTo(getTimestamp(rs, "time_to").getTime());
		}

		return c;
	};

	private Combat getCombat(Integer combatId) throws Exception {
		final String sql;
		if (combatId == null) {
			// last
			sql = SQL_SELECT
					+ " ORDER BY combat_id DESC"
					+ " LIMIT 1";
		} else {
			// exact
			sql = SQL_SELECT
					+ " WHERE combat_id = " + combatId;
		}
		try {
			return getJdbcTemplate().queryForObject(sql, COMBAT_ROW_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Combat> getCombats() throws Exception {
		return getJdbcTemplate().query(
				SQL_SELECT + " ORDER BY combat_id ASC",
				COMBAT_ROW_MAPPER);
	}

	@Override
	public CombatStats getCombatStats(final Combat combat, final CombatSelection combatSel, final String playerName) throws Exception {
		return getCombatStats(combat, combatSel, playerName, false);
	}

	private CombatStats getCombatStats(final Combat combat, final CombatSelection combatSel, final String playerName, final boolean realDuration) throws Exception {

		final Map<String, Object> args = new HashMap<>();
		args.put("playerName", playerName == null ? getCharacterName(combat) : playerName);

		final Long timeFrom;
		final String sql;
		if (combatSel == null) {
			sql = SQL_GET_STATS_SUMS_CACHED;
			args.put("combatId", combat.getCombatId());
			timeFrom = null;

		} else {
			final Boundaries bounds = getBoundaries(combat, combatSel);

			// time from
			args.put("timeFrom", bounds.timeFrom);
			args.put("timeTo", bounds.timeTo);
			timeFrom = bounds.timeFrom.getTime();

			// events
			args.put("eventIdFrom", bounds.eventIdFrom);
			args.put("eventIdTo", bounds.eventIdTo);

			// support for custom conditions (for challenges)
			if (combatSel.getSql() != null) {
				sql = SQL_GET_STATS_SUMS.substring(0, SQL_GET_STATS_SUMS.length() - 1)
						+ " AND " + combatSel.getSql() + ")";
				if (combatSel.getArgs() != null) {
					args.putAll(combatSel.getArgs());
				}
			} else {
				sql = SQL_GET_STATS_SUMS;
			}
		}

		try {
			final CombatStats stats = getJdbcTemplate().query(sql, args, rs -> {
				if (!rs.next()) {
					return null;
				}
				int duration;
				if (realDuration) {
					if (getTimestamp(rs, "time_to") != null && timeFrom != null) {
						duration = (int) Math.max(1000, getTimestamp(rs, "time_to").getTime() - timeFrom);
					} else {
						duration = 1000;
					}
				} else {
					duration = rs.getInt("duration");
				}
				return new CombatStats(
						duration,
						getIntSafe(rs, "actions"),
						getIntSafe(rs, "damage"),
						getIntSafe(rs, "heal"),
						getIntSafe(rs, "effective_heal"),
						getIntSafe(rs, "damage_taken"),
						getIntSafe(rs, "damage_taken"), // total = sub-total
						getIntSafe(rs, "absorbed"),
						getIntSafe(rs, "absorbed"), // total = sub-total
						getIntSafe(rs, "heal_taken"),
						getIntSafe(rs, "effective_heal_taken"),
						getIntSafe(rs, "effective_heal_taken"), // total = sub-total
						getIntSafe(rs, "threat"),
						getIntSafe(rs, "threat_positive"), // not used in total statistics
						rs.getString("discipline") != null ? CharacterDiscipline.valueOf(rs.getString("discipline")) : null);
			});
			if (stats == null && combatSel == null) {
				// NPC perspective = no cached sums
				return getCombatStats(combat, new CombatSelection(combat.getEventIdFrom(), combat.getEventIdTo(), null, null), playerName, realDuration);
			}
			return stats;

		} catch (Exception e) {
			throw new Exception("Unable to get combat summary (" + args + "): " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	@Override
	public List<Event> getCombatEvents(final Combat combat, final Set<Event.Type> filterFlags,
			final Actor filterSource, final Actor filterTarget, final String filterSearch,
			final CombatSelection combatSel, final String playerName) throws Exception {

		final Boundaries bounds = getBoundaries(combat, combatSel);

		final Map<String, Object> args = new HashMap<>();
		args.put("eventIdFrom", bounds.eventIdFrom);
		args.put("eventIdTo", bounds.eventIdTo);

		final StringBuilder
				sqlBuilder = new StringBuilder(),
				pivotsWhere = new StringBuilder();
		sqlBuilder.append(SQL_GET_EVENTS);

		pivotsWhere.setLength(0);

		for (final Event.Type t : filterFlags) {
			switch (t) {
				case DAMAGE_DEALT:
					pivotsWhere.append(" OR (e.source_name = :actorName AND e.effect_guid = " + EntityGuid.Damage + ")");
					break;
				case DAMAGE_TAKEN:
					pivotsWhere.append(" OR (e.target_name = :actorName AND e.effect_guid = " + EntityGuid.Damage + ")");
					break;
				case HEALING_DONE:
					pivotsWhere.append(" OR (e.source_name = :actorName AND e.effect_guid = " + EntityGuid.Heal + ")");
					break;
				case HEALING_TAKEN:
					pivotsWhere.append(" OR (e.target_name = :actorName AND e.effect_guid = " + EntityGuid.Heal + ")");
					break;
				case ACTIONS:
					pivotsWhere.append(" OR (e.source_name = :actorName AND e.effect_guid = " + EntityGuid.AbilityActivate + ")");
					break;
				case EVENT_SELF:
					pivotsWhere.append(" OR (e.target_name = :actorName"
							+ " AND e.effect_guid NOT IN (" + EntityGuid.Heal + ", " + EntityGuid.Damage + ")"
							+ " AND e.action_guid IN (" + EntityGuid.ApplyEffect + ", " + EntityGuid.RemoveEffect + "))");
					break;
				case EVENT_OTHERS:
					pivotsWhere.append(" OR (e.target_name != :actorName"
							+ " AND e.effect_guid NOT IN (" + EntityGuid.Heal + ", " + EntityGuid.Damage + ")"
							+ " AND e.action_guid IN (" + EntityGuid.ApplyEffect + ", " + EntityGuid.RemoveEffect + "))");
				case SIMPLIFIED:
					sqlBuilder.append(" AND e.action_guid NOT IN (" + EntityGuid.Spend
							+ ", " + EntityGuid.TargetSet
							+ ", " + EntityGuid.TargetCleared
							+ ", " + EntityGuid.Restore
							+ ", " + EntityGuid.ModifyCharges + ")");
					break;
				case COMBAT_EXIT:
				case DEATH:
				case NIM_CRYSTAL:
					// nothing
			}
		}

		if (pivotsWhere.length() > 0) {
			sqlBuilder.append(" AND (0=1");
			sqlBuilder.append(pivotsWhere);
			sqlBuilder.append(")");

			if (pivotsWhere.toString().contains(":actorName")) { // XXX
				args.put("actorName", playerName == null ? getCharacterName(combat) : playerName);
			}
		}

		if (filterSource != null) {
			if (filterSource.getInstanceId() != null) {
				sqlBuilder.append(" AND e.source_instance = :sourceInstanceId");
				args.put("sourceInstanceId", filterSource.getInstanceId());
			} else if (filterSource.getGuid() != null) {
				sqlBuilder.append(" AND e.source_guid = :sourceGuid");
				args.put("sourceGuid", filterSource.getGuid());
			} else {
				sqlBuilder.append(" AND e.source_name = :sourceName");
				args.put("sourceName", filterSource.getName());
			}
		}

		if (filterTarget != null) {
			if (filterTarget.getInstanceId() != null) {
				sqlBuilder.append(" AND e.target_instance = :targetInstanceId");
				args.put("targetInstanceId", filterTarget.getInstanceId());
			} else if (filterTarget.getGuid() != null) {
				sqlBuilder.append(" AND e.target_guid = :targetGuid");
				args.put("targetGuid", filterTarget.getGuid());
			} else {
				sqlBuilder.append(" AND e.target_name = :targetName");
				args.put("targetName", filterTarget.getName());
			}
		}

		if (filterSearch != null) {
			final StringBuilder sb = new StringBuilder();
			int i = 0;
			for (final String f : filterSearch.split(" OR [ ]*")) {
				if (sb.length() > 0) {
					sb.append(" OR ");
				}
				sb.append("LOWER(e.source_name) LIKE :fs").append(i)
						.append(" OR e.source_guid LIKE :fs").append(i)
						.append(" OR e.source_instance LIKE :fs").append(i)
						.append(" OR LOWER(e.target_name) LIKE :fs").append(i)
						.append(" OR e.target_guid LIKE :fs").append(i)
						.append(" OR e.target_instance LIKE :fs").append(i)
						.append(" OR LOWER(e.ability_name) LIKE :fs").append(i)
						.append(" OR e.ability_guid LIKE :fs").append(i)
						.append(" OR LOWER(e.effect_name) LIKE :fs").append(i)
						.append(" OR e.effect_guid LIKE :fs").append(i)
						.append(" OR LOWER(e.action_name) LIKE :fs").append(i)
						.append(" OR e.action_guid LIKE :fs").append(i)
						.append(" OR LOWER(e.damage_name) LIKE :fs").append(i)
						.append(" OR e.damage_guid LIKE :fs").append(i)
						.append(" OR LOWER(e.mitigation_name) LIKE :fs").append(i)
						.append(" OR e.mitigation_guid LIKE :fs").append(i)
						.append(" OR LOWER(e.absorption_name) LIKE :fs").append(i)
						.append(" OR e.absorption_guid LIKE :fs").append(i)
						.append(" OR LOWER(e.reflect_name) LIKE :fs").append(i)
						.append(" OR e.reflect_guid LIKE :fs").append(i);
				args.put("fs" + i, "%" + f.toLowerCase() + "%");
				i++;
			}
			sqlBuilder.append(" AND (").append(sb).append(")");
		}

		return getJdbcTemplate().query(sqlBuilder.toString(), args,
				(rs, rowNum) -> {
					Event e = new Event(rs.getInt("event_id"), rs.getInt("log_id"), getTimestamp(rs, "timestamp").getTime());

					if (rs.getString("source_name") != null) {
						e.setSource(context.getActor(rs.getString("source_name"),
								Actor.Type.valueOf(rs.getInt("source_type")),
								getValueOrNull(rs, rs.getLong("source_guid")),
								getValueOrNull(rs, rs.getLong("source_instance"))
						));
					}

					if (rs.getString("target_name") != null) {
						e.setTarget(context.getActor(rs.getString("target_name"),
								Actor.Type.valueOf(rs.getInt("target_type")),
								getValueOrNull(rs, rs.getLong("target_guid")),
								getValueOrNull(rs, rs.getLong("target_instance"))
						));
					}

					if (rs.getString("ability_name") != null) {
						e.setAbility(context.getEntity(rs.getString("ability_name"), rs.getLong("ability_guid")));
					}

					if (rs.getString("action_name") != null) {
						e.setAction(context.getEntity(rs.getString("action_name"), rs.getLong("action_guid")));
					}

					if (rs.getString("effect_name") != null) {
						e.setEffect(context.getEntity(rs.getString("effect_name"), rs.getLong("effect_guid")));
					}

					if (getValueOrNull(rs, rs.getInt("value")) != null) {
						e.setValue(rs.getInt("value"));
						e.setCrit(rs.getBoolean("is_crit"));
					}

					if (rs.getString("damage_name") != null) {
						e.setDamage(context.getEntity(rs.getString("damage_name"), rs.getLong("damage_guid")));
					}

					if (rs.getString("reflect_name") != null) {
						e.setReflect(context.getEntity(rs.getString("reflect_name"), rs.getLong("reflect_guid")));
					}

					if (rs.getString("mitigation_name") != null) {
						e.setMitigation(context.getEntity(rs.getString("mitigation_name"), rs.getLong("mitigation_guid")));
					}

					if (rs.getString("absorption_name") != null) {
						e.setAbsorption(context.getEntity(rs.getString("absorption_name"), rs.getLong("absorption_guid")));
						e.setAbsorbed(getValueOrNull(rs, rs.getInt("absorbed")));

						if (rs.getString("abs_event_id") != null) {
							e.setAbsorptionEventId(rs.getInt("abs_event_id"));
							e.setAbsorptionAbility(context.getEntity(rs.getString("abs_ability_name"), rs.getLong("abs_ability_guid")));
							e.setAbsorptionSource(context.getActor(rs.getString("abs_source_name"), Actor.Type.valueOf(rs.getInt("abs_source_type"))));
						}
					}

					e.setThreat(getValueOrNull(rs, rs.getLong("threat")));
					e.setGuardState(getValueOrNull(rs, rs.getInt("guard_state")));
					e.setEffectiveHeal(getValueOrNull(rs, rs.getInt("effective_heal")));
					e.setEffectiveThreat(getValueOrNull(rs, rs.getLong("effective_threat")));

					return e;
				});
	}

	@Override
	public List<CombatTickStats> getCombatTicks(final Combat combat, final CombatSelection combatSel, final String playerName) throws Exception {

		final Boundaries bounds = getBoundaries(combat, combatSel);

		// recursive SQL does not support "?" binding
		String sql = SQL_GET_STATS_TICKS;

		// resolve boundaries
		sql = sql.replace("%event_from", "event_id = " + bounds.eventIdFrom);
		sql = sql.replace("%time_from", "'" + sdf.format(bounds.timeFrom) + "'");
		sql = sql.replace("%event_to", "s.event_id < " + bounds.eventIdTo); // inclusive (last ID = boundary + 1)
		sql = sql.replace("%playerName", "'" + (playerName == null ? getCharacterName(combat) : playerName).replace("'", "") + "'");

		return getJdbcTemplate().query(sql, (Object[]) null, (rs, rowNum) -> new CombatTickStats(
				getIntSafe(rs, "duration"),
				getIntSafe(rs, "damage"),
				getIntSafe(rs, "dps"),
				getIntSafe(rs, "heal"),
				getIntSafe(rs, "hps"),
				getIntSafe(rs, "effective_heal"),
				getIntSafe(rs, "ehps"),

				getIntSafe(rs, "damage_taken"),
				getIntSafe(rs, "dtps"),

				getIntSafe(rs, "effective_heal_taken"),
				getIntSafe(rs, "health_sub")));
	}

	@Override
	public List<DamageDealtStats> getDamageDealtStatsSimple(final Combat combat, final CombatSelection combatSel, final String playerName) throws Exception {

		final Boundaries bounds = getBoundaries(combat, combatSel);

		final Timestamp lookbehind = new Timestamp(bounds.timeFrom.getTime() - LOOKBEHIND_SECONDS * 1000);

		final Map<String, Object> args = createArgs(bounds);
		args.put("playerName", playerName == null ? getCharacterName(combat) : playerName);
		args.put("behindEventIdFrom", bounds.eventIdFrom - LOOKBEHIND_EVENTS);
		args.put("behindTimeFrom", lookbehind);

		return getJdbcTemplate().query(SQL_GET_DAMAGE_DEALT_SIMPLE, args, (rs, rowNum) -> new DamageDealtStats(
				"Total",
				"Total",
				0L,
				rs.getInt("actions"),
				rs.getInt("ticks"),
				rs.getInt("ticks_normal"),
				rs.getInt("ticks_crit"),
				rs.getInt("ticks_miss"),
				rs.getInt("total"),
				rs.getInt("total_normal"),
				rs.getInt("total_crit"),
				rs.getInt("max"),
				rs.getInt("dps"),
				0L,
				0L,
				0L,
				0L,
				rs.getDouble("pct_crit"),
				rs.getDouble("pct_miss"),
				rs.getString("damage_type"),
				0L,
				0L
		));
	}

	@Override
	public List<DamageDealtStats> getDamageDealtStats(final Combat combat, boolean byTargetType, boolean byTargetInstance, boolean byAbility,
			final CombatSelection combatSel, final String playerName) throws Exception {

		final Boundaries bounds = getBoundaries(combat, combatSel);

		final Timestamp lookbehind = new Timestamp(bounds.timeFrom.getTime() - LOOKBEHIND_SECONDS * 1000);
		final Map<String, Object> args = createArgs(bounds);
		args.put("playerName", playerName == null ? getCharacterName(combat) : playerName);
		args.put("behindEventIdFrom", bounds.eventIdFrom - LOOKBEHIND_EVENTS);
		args.put("behindTimeFrom", lookbehind);

		final StringBuilder
				pivotsCols = new StringBuilder(),
				pivotsWhere = new StringBuilder(),
				pivotsGroup = new StringBuilder();

		if (byTargetInstance) {
			pivotsCols.append(", e.target_name, e.target_instance");
			pivotsWhere.append(" AND e.target_name != :playerName");
			pivotsGroup.append(", e.target_name, e.target_instance");

		} else if (byTargetType) {
			pivotsCols.append(", e.target_name, 0 target_instance");
			pivotsWhere.append(" AND e.target_name != :playerName");
			pivotsGroup.append(", e.target_name");

		} else {
			pivotsCols.append(", 'Total' target_name, 0 target_instance");
		}

		if (byAbility) {
			pivotsCols.append(", e.ability_name, e.ability_guid");
			pivotsGroup.append(", e.ability_name, e.ability_guid");
		} else {
			pivotsCols.append(", 'Total' ability_name, 0 ability_guid");
		}

		return getJdbcTemplate().query(SQL_GET_DAMAGE_DEALT_TOTALS
						.replace("%pivots_cols", pivotsCols.substring(2))
						.replace("%pivots_where", pivotsWhere.length() > 0 ? pivotsWhere.toString() : "")
						.replace("%pivots_group", pivotsGroup.length() > 0 ? pivotsGroup.substring(2) : "1")
				, args,
				(rs, rowNum) -> new DamageDealtStats(
						rs.getString("target_name"),
						(rs.getString("dot_name") != null ? rs.getString("dot_name") + ": " : "") + rs.getString("ability_name"),
						(rs.getString("dot_name") != null ? rs.getLong("dot_guid") : rs.getLong("ability_guid")),
						rs.getString("dot_name") != null ? rs.getInt("dot_actions") : rs.getInt("actions"),
						rs.getInt("ticks"),
						rs.getInt("ticks_normal"),
						rs.getInt("ticks_crit"),
						rs.getInt("ticks_miss"),
						rs.getInt("total"),
						rs.getInt("total_normal"),
						rs.getInt("total_crit"),
						rs.getInt("max"),
						rs.getInt("dps"),
						rs.getDouble("pct_total"),
						rs.getDouble("avg_normal"),
						rs.getDouble("avg_crit"),
						rs.getDouble("avg_hit"),
						rs.getDouble("pct_crit"),
						rs.getDouble("pct_miss"),
						rs.getString("damage_type"),
						Math.max(
								bounds.timeFrom.getTime(),
								getTimestamp(rs, (getTimestamp(rs, "target_time_from") != null ? "target_time_from" : "sub_time_from")).getTime()),
						getTimestamp(rs, (getTimestamp(rs, "target_time_to") != null ? "target_time_to" : "sub_time_to")).getTime()
				));
	}

	@Override
	public List<HealingDoneStats> getHealingDoneStats(final Combat combat, boolean byTarget, boolean byAbility, final CombatSelection combatSel, final String playerName) throws Exception {

		final Boundaries bounds = getBoundaries(combat, combatSel);

		final Timestamp lookbehind = new Timestamp(bounds.timeFrom.getTime() - LOOKBEHIND_SECONDS * 1000);
		final Map<String, Object> args = createArgs(bounds);
		args.put("playerName", playerName == null ? getCharacterName(combat) : playerName);
		args.put("behindEventIdFrom", bounds.eventIdFrom - LOOKBEHIND_EVENTS);
		args.put("behindTimeFrom", lookbehind);

		final StringBuilder
				pivotsCols = new StringBuilder(),
				pivotsWhere = new StringBuilder(),
				pivotsGroup = new StringBuilder();

		if (byTarget) {
			pivotsCols.append(", e.target_name, 0 target_instance");
			pivotsGroup.append(", e.target_name");

		} else {
			pivotsCols.append(", 'Total' target_name, 0 target_instance");
		}

		if (byAbility) {
			pivotsCols.append(", e.ability_name, e.ability_guid");
			pivotsGroup.append(", e.ability_name, e.ability_guid");
		} else {
			pivotsCols.append(", 'Total' ability_name, 0 ability_guid");
		}

		return getJdbcTemplate().query(SQL_GET_HEALING_DONE_TOTALS
						.replace("%pivots_cols", pivotsCols.substring(2))
						.replace("%pivots_where", pivotsWhere.length() > 0 ? pivotsWhere : "")
						.replace("%pivots_group", pivotsGroup.length() > 0 ? pivotsGroup.substring(2) : "1")
				, args,
				(rs, rowNum) -> new HealingDoneStats(
						rs.getString("target_name"),
						(rs.getString("dot_name") != null ? rs.getString("dot_name") + ": " : "") + rs.getString("ability_name"),
						rs.getLong("ability_guid"),
						rs.getString("dot_name") != null ? rs.getInt("dot_actions") : rs.getInt("actions"),
						rs.getInt("ticks"),
						rs.getInt("ticks_normal"),
						rs.getInt("ticks_crit"),
						rs.getInt("total"),
						rs.getInt("max"),
						rs.getInt("total_normal"),
						rs.getInt("total_crit"),
						rs.getInt("total_effective"),
						rs.getInt("hps"),
						rs.getInt("ehps"),
						rs.getDouble("pct_total"),
						rs.getDouble("avg_normal"),
						rs.getDouble("avg_crit"),
						rs.getDouble("pct_crit"),
						rs.getDouble("pct_effective"),
						rs.getInt("aps"),
						rs.getInt("absorbed"),
						getTimestamp(rs, (getTimestamp(rs, "target_time_from") != null ? "target_time_from" : "sub_time_from")).getTime(),
						getTimestamp(rs, (getTimestamp(rs, "target_time_to") != null ? "target_time_to" : "sub_time_to")).getTime()
				));
	}

	@Override
	public CombatMitigationStats getCombatMitigationStats(final Combat combat, final CombatSelection combatSel, final String playerName) throws Exception {

		final Boundaries bounds = getBoundaries(combat, combatSel);

		final Map<String, Object> args = createArgs(bounds);
		args.put("playerName", playerName == null ? getCharacterName(combat) : playerName);

		return getJdbcTemplate().query(SQL_GET_MITIGATION_STATS_SUMS, args, rs -> {
			if (!rs.next()) return null;
			return new CombatMitigationStats(
					rs.getInt("duration"),
					rs.getInt("ticks"),
					rs.getInt("damage"),

					rs.getInt("internal"),
					rs.getDouble("pct_internal"),
					rs.getInt("elemental"),
					rs.getDouble("pct_elemental"),
					rs.getInt("energy"),
					rs.getDouble("pct_energy"),
					rs.getInt("kinetic"),
					rs.getDouble("pct_kinetic"),

					rs.getInt("ticks_shield"),
					rs.getDouble("pct_shield"),
					rs.getInt("ticks_miss"),
					rs.getDouble("pct_miss"),

					rs.getInt("absorbed_self"),
					rs.getDouble("pct_absorbed_self"),
					rs.getInt("absorbed") - rs.getInt("absorbed_self"),
					rs.getDouble("pct_absorbed") - rs.getDouble("pct_absorbed_self"),

					rs.getInt("aps"));
		});
	}

	@Override
	public List<Actor> getCombatActors(final Combat combat, final Actor.Role role, final CombatSelection combatSel) throws Exception {

		final String sql;
		switch (role) {
			case SOURCE:
				sql = SQL_GET_SOURCE_NAMES;
				break;
			default:
				sql = SQL_GET_TARGET_NAMES;
		}

		final Boundaries bounds = getBoundaries(combat, combatSel);

		final Map<String, Object> args = new HashMap<>();
		args.put("eventIdFrom", bounds.eventIdFrom);
		args.put("eventIdTo", bounds.eventIdTo);

		return getJdbcTemplate().query(sql, args, (rs, rowNum) -> {
			Actor a = context.getActor(rs.getString(role == Actor.Role.SOURCE ? "source_name" : "target_name"),
					Actor.Type.valueOf(rs.getInt(role == Actor.Role.SOURCE ? "source_type" : "target_type")),
					getValueOrNull(rs, rs.getLong(role == Actor.Role.SOURCE ? "source_guid" : "target_guid")),
					getValueOrNull(rs, rs.getLong(role == Actor.Role.SOURCE ? "source_instance" : "target_instance"))
			);
			a.setTimeFrom(getTimestamp(rs, role == Actor.Role.SOURCE ? "source_time_from" : "target_time_from").getTime());
			a.setTimeTo(getTimestamp(rs, role == Actor.Role.SOURCE ? "source_time_to" : "target_time_to").getTime());
			return a;
		});
	}

	@Override
	public List<DamageTakenStats> getDamageTakenStats(final Combat combat, boolean bySourceType, boolean bySourceInstance, boolean byAbility,
			final CombatSelection combatSel, final String playerName) throws Exception {

		final Boundaries bounds = getBoundaries(combat, combatSel);

		final Map<String, Object> args = createArgs(bounds);
		args.put("playerName", playerName == null ? getCharacterName(combat) : playerName);

		StringBuilder
				pivotsCols = new StringBuilder(),
				pivotsWhere = new StringBuilder(),
				pivotsGroup = new StringBuilder();

		if (bySourceInstance) {
			pivotsCols.append(", e.source_name, e.source_instance");
			pivotsGroup.append(", e.source_name, e.source_instance");

		} else if (bySourceType) {
			pivotsCols.append(", e.source_name, 0 source_instance");
			pivotsGroup.append(", e.source_name");

		} else {
			pivotsCols.append(", 'Total' source_name, 0 source_instance");
		}

		if (byAbility) {
			pivotsCols.append(", e.ability_name, e.ability_guid");
			pivotsGroup.append(", e.ability_name, e.ability_guid");
		} else {
			pivotsCols.append(", 'Total' ability_name, 0 ability_guid");
		}

		return getJdbcTemplate().query(SQL_GET_DAMAGE_TAKEN_TOTALS
						.replace("%pivots_cols", pivotsCols.substring(2))
						.replace("%pivots_where", pivotsWhere.length() > 0 ? pivotsWhere : "")
						.replace("%pivots_group", pivotsGroup.length() > 0 ? pivotsGroup.substring(2) : "1")
				, args,
				(rs, rowNum) -> new DamageTakenStats(
						rs.getString("source_name"),
						rs.getString("ability_name"),
						rs.getLong("ability_guid"),
						rs.getInt("ticks"),
						rs.getInt("ticks_shield"),
						rs.getInt("ticks_miss"),
						rs.getInt("total"),
						rs.getInt("total_ie"),
						rs.getInt("max"),
						rs.getInt("total_absorbed"),
						rs.getInt("dtps"),
						rs.getDouble("pct_total"),
						rs.getDouble("avg_normal"),
						rs.getDouble("pct_shield"),
						rs.getDouble("pct_miss"),
						rs.getString("damage_type"),
						getTimestamp(rs, (getTimestamp(rs, "source_time_from") != null ? "source_time_from" : "sub_time_from")).getTime(),
						getTimestamp(rs, (getTimestamp(rs, "source_time_to") != null ? "source_time_to" : "sub_time_to")).getTime()
				));
	}

	@Override
	public List<HealingTakenStats> getHealingTakenStats(final Combat combat, boolean bySource, boolean byAbility, final CombatSelection combatSel, final String playerName) throws Exception {

		final Boundaries bounds = getBoundaries(combat, combatSel);

		final Map<String, Object> args = createArgs(bounds);
		args.put("playerName", playerName == null ? getCharacterName(combat) : playerName);

		StringBuilder
				pivotsCols = new StringBuilder(),
				pivotsWhere = new StringBuilder(),
				pivotsGroup = new StringBuilder();

		if (bySource) {
			pivotsCols.append(", e.source_name, 0 source_instance");
			pivotsGroup.append(", e.source_name");

		} else {
			pivotsCols.append(", 'Total' source_name, 0 source_instance");
		}

		if (byAbility) {
			pivotsCols.append(", e.ability_name, e.ability_guid");
			pivotsGroup.append(", e.ability_name, e.ability_guid");
		} else {
			pivotsCols.append(", 'Total' ability_name, 0 ability_guid");
		}

		return getJdbcTemplate().query(SQL_GET_HEALING_TAKEN_TOTALS
						.replace("%pivots_cols", pivotsCols.substring(2))
						.replace("%pivots_where", pivotsWhere.length() > 0 ? pivotsWhere : "")
						.replace("%pivots_group", pivotsGroup.length() > 0 ? pivotsGroup.substring(2) : "1")
				, args,
				(rs, rowNum) -> new HealingTakenStats(
						rs.getString("source_name"),
						rs.getString("ability_name"),
						rs.getLong("ability_guid"),
						rs.getInt("ticks"),
						rs.getInt("ticks_normal"),
						rs.getInt("ticks_crit"),
						rs.getInt("total"),
						rs.getInt("total_normal"),
						rs.getInt("total_crit"),
						rs.getInt("total_effective"),
						rs.getInt("htps"),
						rs.getInt("ehtps"),
						rs.getDouble("pct_total"),
						rs.getDouble("avg_normal"),
						rs.getDouble("avg_crit"),
						rs.getDouble("pct_crit"),
						rs.getDouble("pct_effective"),
						rs.getInt("aps"),
						rs.getInt("absorbed"),
						getTimestamp(rs, (getTimestamp(rs, "source_time_from") != null ? "source_time_from" : "sub_time_from")).getTime(),
						getTimestamp(rs, (getTimestamp(rs, "source_time_to") != null ? "source_time_to" : "sub_time_to")).getTime()
				));
	}

	@Override
	public List<AbsorptionStats> getAbsorptionStats(final Combat combat, final CombatSelection combatSel, final String playerName) throws Exception {

		final Boundaries bounds = getBoundaries(combat, combatSel);

		final Map<String, Object> args = new HashMap<>();
		args.put("eventIdFrom", bounds.eventIdFrom);
		args.put("eventIdTo", bounds.eventIdTo);
		args.put("playerName", playerName == null ? getCharacterName(combat) : playerName);

		return getJdbcTemplate().query(SQL_GET_ABSORPTION_TAKEN_TOTALS,
				args,
				(rs, rowNum) -> new AbsorptionStats(
						rs.getString("source_name"),
						EntityGuid.fromGuid(rs.getLong("effect_guid"), rs.getString("effect_name")),
						rs.getInt("total")
				));
	}

	@Override
	public List<ChallengeStats> getCombatChallengeStats(final Combat combat, final CombatSelection combatSel, final String playerName) throws Exception {

		if ((combat.getBoss() == null)
				|| ((availableChallenges = combat.getBoss().getRaid().getChallenges(combat.getBoss())) == null)) {
			return null;
		}

		phasesToChallenges.clear();
		for (final CombatChallenge ch : availableChallenges) {
			phasesToChallenges.put(ch.getPhaseName(), ch);
		}

		final Map<String, Object> args = new HashMap<>();
		args.put("combatId", combat.getCombatId());
		args.put("tickFrom", combatSel != null && combatSel.getTickFrom() != null ? combatSel.getTickFrom() : 0);
		args.put("tickTo", combatSel != null && combatSel.getTickTo() != null ? combatSel.getTickTo() : Integer.MAX_VALUE);
		args.put("phaseNames", new ArrayList<>(phasesToChallenges.keySet()));

		return getJdbcTemplate().query(SQL_GET_COMBAT_CHALLENGES,
				args,
				new RowMapper<ChallengeStats>() {
					Long tickFrom, tickTo;
					boolean noCache = false;

					@Override
					public ChallengeStats mapRow(ResultSet rs, int rowNum) {
						try {
							noCache = false;
							if (combatSel != null) {
								if (combatSel.getTickFrom() != null && (combatSel.getTickFrom() > rs.getLong("tick_from"))) {
									tickFrom = combatSel.getTickFrom();
									noCache = true;
								} else {
									tickFrom = rs.getLong("tick_from");
								}
								if (combatSel.getTickTo() != null && (getValueOrNull(rs, rs.getLong("tick_to")) == null || context.getTickTo() < rs.getLong("tick_to"))) {
									tickTo = combatSel.getTickTo();
									noCache = true;
								} else {
									tickTo = getValueOrNull(rs, rs.getLong("tick_to"));
									noCache = noCache || tickTo == null;
								}
							} else {
								tickFrom = rs.getLong("tick_from");
								tickTo = getValueOrNull(rs, rs.getLong("tick_to"));
							}

							if (!noCache && cachedChallenges.containsKey(combat.getCombatId())) {
								if (cachedChallenges.get(combat.getCombatId()).containsKey(playerName)) {
									if (cachedChallenges.get(combat.getCombatId()).get(playerName).containsKey(rs.getLong("tick_from"))) {
										return cachedChallenges.get(combat.getCombatId()).get(playerName).get(rs.getLong("tick_from"));
										// NOTREACHED
									}
								}
							}
							final CombatSelection challengeCombatSel = new CombatSelection(
									rs.getInt("event_id_from"),
									getValueOrNull(rs, rs.getInt("event_id_to")),
									tickFrom,
									tickTo,
									phasesToChallenges.get(rs.getString("name")).getArgs(),
									phasesToChallenges.get(rs.getString("name")).getSql());

							final CombatStats stats = getCombatStats(combat, challengeCombatSel, playerName, true);

							final ChallengeStats challengeStats = new ChallengeStats(
									phasesToChallenges.get(rs.getString("name")).getChallengeName(),
									tickFrom,
									tickFrom + stats.getTick(),
									stats.getDamage(),
									stats.getHeal(),
									stats.getEffectiveHeal());

							if (!noCache && (getValueOrNull(rs, rs.getLong("tick_to")) != null)) {
								if (!cachedChallenges.containsKey(combat.getCombatId())) {
									cachedChallenges.put(combat.getCombatId(), new HashMap<>());
								}
								if (!cachedChallenges.get(combat.getCombatId()).containsKey(playerName)) {
									cachedChallenges.get(combat.getCombatId()).put(playerName, new HashMap<>());
								}
								cachedChallenges.get(combat.getCombatId()).get(playerName).put(rs.getLong("tick_from"), challengeStats);
							}

							return challengeStats;

						} catch (Exception e) {
							logger.error("Unable to get combat challenge: " + e.getMessage(), e);
							return null;
						}
					}
				});
	}

	@Override
	public List<Effect> getCombatEffects(final Combat combat, final CombatSelection combatSel) throws Exception {

		final Boundaries bounds = getBoundaries(combat, combatSel);

		final Map<String, Object> args = new HashMap<>();
		args.put("eventIdFrom", bounds.eventIdFrom);
		args.put("eventIdTo", bounds.eventIdTo);

		return getJdbcTemplate().query(SQL_GET_COMBAT_EFFECTS,
				args,
				(rs, rowNum) -> {
					final Effect effect = new Effect(
							rs.getInt("effect_id"),
							rs.getInt("event_id_from"),
							getTimestamp(rs, "time_from").getTime(),
							context.getActor(rs.getString("source_name"), Actor.Type.valueOf(rs.getInt("source_type"))),
							context.getActor(rs.getString("target_name"), Actor.Type.valueOf(rs.getInt("target_type"))),
							context.getEntity(rs.getString("ability_name"), rs.getLong("ability_guid")),
							context.getEntity(rs.getString("effect_name"), rs.getLong("effect_guid")),
							rs.getBoolean("is_activated"),
							rs.getBoolean("is_absorption")
					);
					if (rs.getInt("event_id_to") > 0) {
						effect.setEventIdTo(rs.getInt("event_id_to"));
						effect.setTimeTo(getTimestamp(rs, "time_to").getTime());
					}
					return effect;
				});
	}

	private int getIntSafe(ResultSet rs, String col) {
		try {
			return rs.getInt(col);
		} catch (Exception e) {
			try {
				return (int) Math.min(Math.max(rs.getLong(col), Integer.MIN_VALUE), Integer.MAX_VALUE);

			} catch (Exception e2) {
				throw new IllegalArgumentException("Unable to read value " + col + ": " + e2.getMessage());
			}
		}
	}

	private Timestamp getTimestamp(ResultSet rs, String col) throws SQLException {
		return rs.getTimestamp(col, cal);
	}

	private String getCharacterName(final Combat combat) throws Exception {
		try {
			final String name = getJdbcTemplate().queryForObject(
					"SELECT character_name FROM logs WHERE log_id = :logId",
					Collections.singletonMap("logId", combat.getLogId()), String.class);
			logger.warn("Emergency character name resolve: " + name + " at " + combat);
			return name;
		} catch (EmptyResultDataAccessException e) {
			logger.error("No character found for " + combat + ", using " + context.getSelectedPlayer());
			return context.getSelectedPlayer(); // will not show anything, but would not fail either
		}
	}

	private Map<String, Object> createArgs(final Boundaries bounds) {
		final Map<String, Object> args = new HashMap<>();
		if (bounds != null) {
			args.put("eventIdFrom", bounds.eventIdFrom);
			args.put("eventIdTo", bounds.eventIdTo);
			args.put("timeFrom", bounds.timeFrom);
			args.put("timeTo", bounds.timeTo);
		}
		return args;
	}

	@Override
	public void reset() throws Exception {
		cachedChallenges.clear();
		sdf.setTimeZone(TimeUtils.getCurrentTimezone());
		cal.setTimeZone(TimeUtils.getCurrentTimezone());
		getJdbcTemplate().execute("TRUNCATE TABLE combats");
		getJdbcTemplate().execute("TRUNCATE TABLE combat_stats");
	}
}
