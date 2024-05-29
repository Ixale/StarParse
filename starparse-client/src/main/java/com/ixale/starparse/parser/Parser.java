package com.ixale.starparse.parser;

import com.ixale.starparse.domain.Absorption;
import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.AttackType;
import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.CharacterRole;
import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatInfo;
import com.ixale.starparse.domain.CombatLog;
import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.EffectKey;
import com.ixale.starparse.domain.Entity;
import com.ixale.starparse.domain.EntityGuid;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.LocationInfo;
import com.ixale.starparse.domain.Phase;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.Raid.Mode;
import com.ixale.starparse.domain.Raid.Size;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBoss.BossUpgrade;
import com.ixale.starparse.service.impl.Context;
import com.ixale.starparse.time.TimeUtils;
import com.ixale.starparse.timer.BaseTimer;
import com.ixale.starparse.timer.TimerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ixale.starparse.parser.Helpers.getDiscipline;
import static com.ixale.starparse.parser.Helpers.getRaidBoss;
import static com.ixale.starparse.parser.Helpers.isAbilityEqual;
import static com.ixale.starparse.parser.Helpers.isAbilityFakeHeal;
import static com.ixale.starparse.parser.Helpers.isAbilityGeneric;
import static com.ixale.starparse.parser.Helpers.isAbilityNoThreat;
import static com.ixale.starparse.parser.Helpers.isAbilityNonreducedThreat;
import static com.ixale.starparse.parser.Helpers.isActionApply;
import static com.ixale.starparse.parser.Helpers.isActionRemove;
import static com.ixale.starparse.parser.Helpers.isEffectAbilityActivate;
import static com.ixale.starparse.parser.Helpers.isEffectAbsorption;
import static com.ixale.starparse.parser.Helpers.isEffectCombatDrop;
import static com.ixale.starparse.parser.Helpers.isEffectDamage;
import static com.ixale.starparse.parser.Helpers.isEffectDeath;
import static com.ixale.starparse.parser.Helpers.isEffectDualWield;
import static com.ixale.starparse.parser.Helpers.isEffectEnterCombat;
import static com.ixale.starparse.parser.Helpers.isEffectEqual;
import static com.ixale.starparse.parser.Helpers.isEffectExitCombat;
import static com.ixale.starparse.parser.Helpers.isEffectGeneric;
import static com.ixale.starparse.parser.Helpers.isEffectGuard;
import static com.ixale.starparse.parser.Helpers.isEffectHeal;
import static com.ixale.starparse.parser.Helpers.isEffectLoginImmunity;
import static com.ixale.starparse.parser.Helpers.isEffectPvP;
import static com.ixale.starparse.parser.Helpers.isEffectRevive;
import static com.ixale.starparse.parser.Helpers.isSourceOtherPlayer;
import static com.ixale.starparse.parser.Helpers.isSourceThisPlayer;
import static com.ixale.starparse.parser.Helpers.isTargetAnyPlayer;
import static com.ixale.starparse.parser.Helpers.isTargetOtherPlayer;
import static com.ixale.starparse.parser.Helpers.isTargetThisPlayer;

public class Parser {

	private static final Logger logger = LoggerFactory.getLogger(Parser.class);

	// combat_2014-01-26_21_17_31_474300
	private final static Pattern filePattern = Pattern
			.compile("^combat_(?<Year>\\d{4})-(?<Month>\\d{2})-(?<Day>\\d{2})_(?<HH>\\d{2})_\\d{2}_\\d{2}_\\d{6}\\.txt$");
	private Matcher fileMatcher;

	private Pattern basePattern, combatPattern, zonePattern, disciplinePattern, ignorePattern, gsfPattern; // version dependent
	private boolean isEffectiveLogged;

	private final static String TIMESTAMP = "(?<TimeStamp>(?<HH>\\d{2}):(?<MM>\\d{2}):(?<SS>\\d{2})\\.(?<MS>\\d{3}))";

	private final static String SOURCE = "(?<Source>|@(?<SourcePlayer>(?<SourcePlayerName>[^#]*)#(?<SourcePlayerId>\\d*))(|/(?<SourceCompanionName>[^{]*) \\{(?<SourceCompanionGuid>\\d*)}(|:(?<SourceCompanionInstance>\\d*)))?|(?<SourceNpcName>[^{]*) \\{(?<SourceNpcGuid>\\d*)}(|:(?<SourceNpcInstance>\\d*)))";
	private final static String SOURCE_V = "(?<SourceVector>(?<SourceX>[0-9.\\-]+),(?<SourceY>[0-9.\\-]+),(?<SourceZ>[0-9.\\-]+),(?<SourceAngle>[0-9.\\-]+))";
	private final static String SOURCE_HP = "(?<SourceHp>(?<SourceCurrentHp>\\d+)/(?<SourceMaxHp>\\d+))";

	private final static String TARGET = "(?<Target>|=|@(?<TargetPlayer>(?<TargetPlayerName>[^#]*)#(?<TargetPlayerId>\\d*))(|/(?<TargetCompanionName>[^{]*) \\{(?<TargetCompanionGuid>\\d*)}(|:(?<TargetCompanionInstance>\\d*)))?|(?<TargetNpcName>[^{]*) \\{(?<TargetNpcGuid>\\d*)}(|:(?<TargetNpcInstance>\\d*)))";
	private final static String TARGET_V = "(?<TargetVector>(?<TargetX>[0-9.\\-]+),(?<TargetY>[0-9.\\-]+),(?<TargetZ>[0-9.\\-]+),(?<TargetAngle>[0-9.\\-]+))";
	private final static String TARGET_HP = "(?<TargetHp>(?<TargetCurrentHp>\\d+)/(?<TargetMaxHp>\\d+))";

	private final static String ABILITY = "(?<Ability>|(?<AbilityName>[^{]*) \\{(?<AbilityGuid>\\d*)})";
	private final static String ACTION = "(?<ActionName>[^{]*) \\{(?<ActionGuid>\\d*)}";

	private final static String BASE = "^\\[" + TIMESTAMP + "] \\[" + SOURCE + "(\\|\\(" + SOURCE_V + "\\)\\|\\(" + SOURCE_HP + "\\))?] " + "\\[" + TARGET + "(\\|\\(" + TARGET_V + "\\)\\|\\(" + TARGET_HP + "\\))?] " + "\\[" + ABILITY + "] \\[(" + ACTION;

	private final static Pattern v7DisciplinePattern = Pattern.compile(BASE
			+ ": (?<AdvancedClass>[^{]*) \\{(?<AdvancedClassGuid>\\d*)}/(?<Discipline>[^{]*) \\{(?<DisciplineGuid>\\d*)})]");

	private final static Pattern v7ZonePattern = Pattern.compile(BASE
			+ ": (?<InstanceName>[^{]*) \\{(?<InstanceGuid>\\d*)}( (?<InstanceType>[^{]+) \\{(?<InstanceTypeGuid>\\d*)})?]"
			+ " \\((?<ServerId>[^)]*)\\)"
			+ " <v(?<Version>(?<Major>\\d*)\\.(?<Minor>\\d*)\\.(?<Revision>[0-9a-z]*)))>");

	// combat log line pattern
	private final static Pattern legacyBasePattern = Pattern.compile("^"
			+ "\\[" + TIMESTAMP + "]"
			+ " \\[(?<Source>"
			+ "|@(?<SourcePlayerName>[^]:]*)(:(?<SourceCompanionName>[^{]*) \\{(?<SourceCompanionGuid>\\d*)})?"
			+ "|(?<SourceNpcName>[^{]*) \\{(?<SourceNpcGuid>\\d*)}(|:(?<SourceNpcInstance>\\d*))"
			+ ")]"
			+ " \\[(?<Target>"
			+ "|@(?<TargetPlayerName>[^]:]*)(|:(?<TargetCompanionName>[^{]*) \\{(?<TargetCompanionGuid>\\d*)})"
			+ "|(?<TargetNpcName>[^{]*) \\{(?<TargetNpcGuid>\\d*)}(|:(?<TargetNpcInstance>\\d*))"
			+ ")]"
			+ " \\[(?<Ability>"
			+ "|(?<AbilityName>[^{]*) \\{(?<AbilityGuid>\\d*)}"
			+ ")]"
			+ " \\[("
			+ "(?<ActionName>[^{]*) \\{(?<ActionGuid>\\d*)}: (?<EffectName>[^{]*) \\{(?<EffectGuid>\\d*)}"
			+ ")]"
			+ " \\("
			+ "(?<Value>-?\\d+)?(?<IsCrit>\\*)? ?"
			+ "( (?<DamageType>[^ \\-]+) \\{(?<DamageTypeGuid>\\d+)})?"
			+ "(\\((?<ReflectType>[^ ]+) \\{(?<ReflectTypeGuid>\\d+)}\\))?"
			+ "(?<IsMitigation> -((?<MitigationType>[^ ]+) \\{(?<MitigationTypeGuid>\\d+)})?)?"
			+ "( \\((?<AbsorbValue>\\d+) (?<AbsorbType>[^ ]+) \\{(?<AbsorbTypeGuid>\\d+)}\\))?"
			+ "\\)"
			+ "($| <(?<Threat>[^>]*?)>)");

	private final static Pattern v7BasePattern = Pattern.compile(BASE
			+ ": (?<EffectName>[^{]*) \\{(?<EffectGuid>\\d*)}"
			+ ")]"
			+ "( \\(((?<Value>-?\\d+)(\\.0)?)?(?<IsCrit>\\*)? ?(~(?<Effective>-?\\d*))? ?"
			+ "( (?<DamageType>[^ \\-]+) \\{(?<DamageTypeGuid>\\d+)})?"
			+ "(\\((?<ReflectType>[^ ]+) \\{(?<ReflectTypeGuid>\\d+)}\\))?"
			+ "(?<IsMitigation> -((?<MitigationType>[^ ]+) \\{(?<MitigationTypeGuid>\\d+)})?)?"
			+ "( \\((?<AbsorbValue>\\d+) (?<AbsorbType>[^ ]+) \\{(?<AbsorbTypeGuid>\\d+)}\\))?"
			+ "\\))?"
			+ "($| <(?<Threat>[^>]*?)(\\.0)?>)?");

	// combat log line pattern for enter/exit combat since 5.4
	private final static Pattern legacyCombatPattern = Pattern.compile("^"
			+ "(\\[(?<Vector>\\{(?<X>[0-9.\\-]+),(?<Z>[0-9.\\-]+),(?<Y>[0-9.\\-]+)})] )?" // v7.0.0
			+ "\\[" + TIMESTAMP + "]"
			+ " \\[(?<Source>"
			+ "|@(?<SourcePlayerName>[^#/]*)(?<SourcePlayerId>#\\d+)?(/(?<SourceCompanionName>[^{]*) \\{(?<SourceCompanionGuid>\\d*)}(:(?<SourceCompanionId>\\d+))?)?"
			+ "|(?<SourceNpcName>[^{]*) \\{(?<SourceNpcGuid>\\d*)}(|:(?<SourceNpcInstance>\\d*))"
			+ ")]"
			+ " \\[(?<Target>"
			+ "|@(?<TargetPlayerName>[^#/]*)(?<TargetPlayerId>#\\d+)?(/(?<TargetCompanionName>[^{]*) \\{(?<TargetCompanionGuid>\\d*)}(:(?<TargetCompanionId>\\d+))?)?"
			+ "|(?<TargetNpcName>[^{]*) \\{(?<TargetNpcGuid>\\d*)}(|:(?<TargetNpcInstance>\\d*))"
			+ ")]"
			+ " \\[(?<Ability>"
			+ "|(?<AbilityName>[^{]*) \\{(?<AbilityGuid>\\d*)}"
			+ ")]"
			+ " \\[("
			+ "(?<ActionName>[^{]*) \\{(?<ActionGuid>\\d*)}: (?<EffectName>[^{]*) \\{(?<EffectGuid>(836045448945489|836045448945490))}"
			+ ")]"
			+ " \\((?<Value>.*)\\)"
			+ "($| <(?<Threat>[^>]*?)>)");

	// combat log line pattern for GSF only (fallback)
	private final static Pattern legacyGsfPattern = Pattern.compile("^"
			+ "\\[" + TIMESTAMP + "]"
			+ " \\[(?<SourceGsfName>|\\d{10,})]"
			+ " \\[(?<TargetGsfName>|\\d{10,})] .*");

	private final static Pattern v7GsfPattern = Pattern.compile("^"
			+ "\\[" + TIMESTAMP + "]"
			+ " \\[(?<SourceGsfName>|=|::\\d{10,}::\\|[^]]*)]"
			+ " \\[(?<TargetGsfName>|=|::\\d{10,}::\\|[^]]*)] .*");

	// healing threat ratios
	private static final double THREAT_HEAL = .5,
			THREAT_HEAL_REDUCTION = .1, // 10% reduction on healers
			THREAT_TANK = 2, // 200% for tanks (even for their self heals / medpacs)
			THREAT_GUARD = .75;

	private static final long COMBAT_DELAY_WINDOW = 4 * 1000, // window to 1) include any lagged damage or healing 2) detect combat drop abilities 3) reconnect "shattered" combats
			COMBAT_REVIVE_WINDOW = 60 * 1000, // window to use revive after death
			COMBAT_RETURN_WINDOW = 30 * 1000, // window to re-enter the combat after revival or combat drop
			EFFECT_OVERLAP_TOLERANCE = 500, // start A ... (start B ... end A ~ within 0.5s) ... end B
			ABSORPTION_OUTSIDE_DELAY_WINDOW = 4 * 1000, // window to include lagged absorption after the effect has ended
			ABSORPTION_INSIDE_DELAY_WINDOW = 500, // effect A ... effect B ... (end A ... absorption A ~ within 0.5s) ... end B ...
			PHASE_DAMAGE_WINDOW = 7 * 1000, // if no damage even occurs within 5s, create close the "damage phase"
			PHASE_DAMAGE_MININUM = 5 * 1000,
			HEALING_THREAT_TOLERANCE = 5;

	private Calendar c;
	private int lastHour;

	// parsed
	private final ArrayList<Event> events = new ArrayList<>();
	private final ArrayList<Combat> combats = new ArrayList<>();
	private final ArrayList<Effect> effects = new ArrayList<>();
	private final ArrayList<Absorption> absorptions = new ArrayList<>();
	private final ArrayList<Phase> phases = new ArrayList<>();

	private final Context context;

	// combat log
	private int combatLogId;
	private CombatLog combatLog;

	// events
	private int eventId;

	// effects
	private int effectId;
	private final ArrayList<Effect> currentEffects = new ArrayList<>();

	private final HashMap<EffectKey, List<Effect>> runningEffects = new HashMap<>();
	private final HashMap<EffectKey, Integer> stackedEffects = new HashMap<>();
	private final HashMap<Effect, Integer> chargedEffects = new HashMap<>();
	private final ArrayList<Long> activatedAbilities = new ArrayList<>();

	// absorptions
	private final Map<Actor, List<Effect>> absorptionEffectsRunning = new HashMap<>();
	private final Map<Actor, List<Effect>> absorptionEffectsClosing = new HashMap<>();
	private final Map<Actor, List<Effect>> absorptionEffectsConsumed = new HashMap<>();
	private final Map<Actor, List<Integer>> absorptionEventsInside = new HashMap<>();
	private final Map<Actor, List<Integer>> absorptionEventsOutside = new HashMap<>();
	private final List<Effect> absorptionEffectsJustClosed = new ArrayList<>();

	// phases
	private int phaseId;
	private Phase currentBossPhase;
	private Event firstDamageEvent, lastDamageEvent, lastCombatDropEvent;

	// combat
	private int combatId;
	private Combat combat;
	private Long combatConnectLimit;
	private BossUpgrade combatBossUpgrade;
	private String instanceName;
	private Long instanceGuid;
	private Raid.Mode instanceMode;
	private Raid.Size instanceSize;
	private boolean isUsingMimCrystal = false;

	public static class ActorState {

		// effective guard state (0, 1, 2)
		public int guarded = 0;
		// detected role
		public CharacterDiscipline discipline; // for SELF, this equals Combat.discipline
		public CharacterRole role = null; // assume healer by default (worst case scenario = more EHPS for off-healing DPS)
		// healing stacks
		public int hotStacks = 0;
		public Long hotSince = null;
		public Entity hotEffect = null;
		public Integer hotDuration = null;
		public Long hotLast = null;

		// effective threat for the current fight
		public long combatTotalThreat;
		// to support mara/jugg and sent/guard distinction (used for player only)
		public boolean isDualWield = false;

	}

	private final HashMap<Actor, ActorState> actorStates = new HashMap<>();
	private Entity pendingHealAbility = null;
	private int hotCount = 0, hotTotal = 0, hotAverage = 0;

	public Parser(final Context context) {
		this.context = context;
	}

	public void reset() {
		combatLogId = 0;
		combatLog = null;
		c = Calendar.getInstance(TimeUtils.getCurrentTimezone());
		lastHour = 0;

		eventId = 0;
		events.clear();

		combatId = 0;
		combat = null;
		combatConnectLimit = null;
		combatBossUpgrade = null;
		instanceName = null;
		instanceGuid = null;
		instanceMode = null;
		instanceSize = null;
		combats.clear();

		effectId = 0;
		runningEffects.clear();
		stackedEffects.clear();
		chargedEffects.clear();
		currentEffects.clear();
		effects.clear();
		activatedAbilities.clear();

		absorptionEffectsRunning.clear();
		absorptionEffectsClosing.clear();
		absorptionEffectsConsumed.clear();
		absorptionEventsInside.clear();
		absorptionEventsOutside.clear();
		absorptionEffectsJustClosed.clear();
		absorptions.clear();

		phaseId = 0;
		currentBossPhase = null;
		firstDamageEvent = lastDamageEvent = null;
		phases.clear();

		actorStates.clear();

//		combatTotalThreat = 0;
//		isDualWield = false;
		pendingHealAbility = null;
		hotCount = hotTotal = hotAverage = 0;

		if (logger.isDebugEnabled()) {
			logger.debug("Context cleared");
		}
	}

	public CombatLog getCombatLog() {
		return combatLog;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	public ArrayList<Combat> getCombats() {
		return combats;
	}

	public Combat getCurrentCombat() {
		return combat;
	}

	public ArrayList<Effect> getEffects() {
		return effects;
	}

	public ArrayList<Effect> getCurrentEffects() {
		return currentEffects;
	}

	public ArrayList<Absorption> getAbsorptions() {
		return absorptions;
	}

	public Phase getCurrentPhase() {
		return currentBossPhase;
	}

	public ArrayList<Phase> getPhases() {
		return phases;
	}

	public Map<Actor, ActorState> getActorStates() {
		return actorStates;
	}

	public void setCombatLogFile(File logFile) throws Exception {
		// new file started
		if (events.size() > 0 || combats.size() > 0 || combat != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Setting new file without finishing the last, discarding: " + events.size() + " events and " + combats.size() + " combats");
			}
		}

		// reset everything
		reset();

		// setup date
		fileMatcher = filePattern.matcher(logFile.getName());

		if (fileMatcher.matches()) {
			c.set(Calendar.YEAR, Integer.parseInt(fileMatcher.group("Year")));
			c.set(Calendar.MONTH, Integer.parseInt(fileMatcher.group("Month")) - 1);
			c.set(Calendar.DATE, Integer.parseInt(fileMatcher.group("Day")));
			lastHour = Integer.parseInt(fileMatcher.group("HH"));
		} else {
			// probably custom name (e.g. "420 parse 360 scope.txt")
			c.setTimeInMillis(logFile.lastModified());
		}

		// FIXME: year 1472
		if (c.get(Calendar.YEAR) < 1900) {
			c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		}

		combatLog = new CombatLog(++combatLogId, logFile.getCanonicalPath(), c.getTimeInMillis());
	}

	public void closeCombatLogFile() {
		if (combat != null) {
			closeCurrentCombat();
		}
	}

	public boolean parseLogLine(final String line) throws ParserException {

		if (line == null) {
			logger.warn("Empty line, ignoring");
			return false;
		}

		if (combatLog == null) {
			throw new RuntimeException("Enclosing combat log not set");
		}

		// match the combat log line
		Matcher baseMatcher;
		if (context.getVersion() == null) {
			baseMatcher = v7ZonePattern.matcher(line);
			if (baseMatcher.matches()) {
				final long timestamp = getTimestamp(baseMatcher);
				context.setVersion(baseMatcher.group("Version"));
				context.setServerId(baseMatcher.group("ServerId"));
				setCharacterName(getSourceActor(baseMatcher, timestamp, null), timestamp);
				if (baseMatcher.group("InstanceName") != null && !baseMatcher.group("InstanceName").isEmpty()) {
					parseInstanceType(
							baseMatcher.group("InstanceName"),
							baseMatcher.group("InstanceGuid"),
							baseMatcher.group("InstanceType"),
							baseMatcher.group("InstanceTypeGuid"),
							timestamp);
				}

			} else {
				// cropped?
				for (final Pattern v7pattern : Arrays.asList(v7BasePattern, v7ZonePattern, v7DisciplinePattern)) {
					baseMatcher = v7pattern.matcher(line);
					if (baseMatcher.matches()) {
						// tentative, inconclusive
						return false;
					}
				}
			}
			if (context.getVersion() == null) {
				// give up
				context.setVersion("6.0.0"); // legacy - default
			}

			if (context.getVersion().startsWith("7")) {
				basePattern = v7BasePattern;
				combatPattern = null;
				zonePattern = v7ZonePattern;
				disciplinePattern = v7DisciplinePattern;
				ignorePattern = null;
				isEffectiveLogged = true;
				gsfPattern = v7GsfPattern;

			} else {
				basePattern = legacyBasePattern;
				combatPattern = legacyCombatPattern;
				zonePattern = null;
				disciplinePattern = null;
				ignorePattern = null;
				isEffectiveLogged = false;
				gsfPattern = legacyGsfPattern;
			}

			if (baseMatcher.matches()) {
				logger.info("Version detected as " + context.getVersion());
				return false;
			}
		}

		baseMatcher = basePattern.matcher(line);

		if (!baseMatcher.matches()) {
			if (combatPattern != null) {
				// fallback to combat enter/exit
				baseMatcher = combatPattern.matcher(line);
			}
			if (!baseMatcher.matches()) {
				if (disciplinePattern != null) {
					baseMatcher = disciplinePattern.matcher(line);
					if (baseMatcher.matches()) {
						final long timestamp = getTimestamp(baseMatcher);
						final Actor a = getSourceActor(baseMatcher, timestamp, null);
						final CharacterDiscipline newDiscipline = CharacterDiscipline.fromGuid(baseMatcher.group("DisciplineGuid"));
						if (newDiscipline != null) {
							if (!Objects.equals(newDiscipline, a.getDiscipline())) {
								if (logger.isDebugEnabled()) {
									logger.debug(a + ": Discipline set as [" + newDiscipline + "] (was " + a.getDiscipline() + ") at " + Event.formatTs(getTimestamp(baseMatcher)));
								}
								a.setDiscipline(newDiscipline);
								if (Actor.Type.SELF.equals(a.getType())) {
									clearHotsTracking();
								}
							}
							if (actorStates.containsKey(a)) {
								actorStates.get(a).discipline = a.getDiscipline();
								actorStates.get(a).role = a.getDiscipline().getRole();
							}
						}
						return false;
					}
				}
				if (zonePattern != null) {
					baseMatcher = zonePattern.matcher(line);
					if (baseMatcher.matches()) {
						final long timestamp = getTimestamp(baseMatcher);
						if (baseMatcher.group("InstanceName") != null && !baseMatcher.group("InstanceName").isEmpty()) {
							parseInstanceType(
									baseMatcher.group("InstanceName"),
									baseMatcher.group("InstanceGuid"),
									baseMatcher.group("InstanceType"),
									baseMatcher.group("InstanceTypeGuid"),
									timestamp);
						}
						return false;
					}
				}
				if (gsfPattern.matcher(line).matches()) {
					// GSF combat line, ignore for now
					return false;
				}
				if (ignorePattern != null && ignorePattern.matcher(line).matches()) {
					// intentionally ignored
					return false;
				}
				throw new ParserException("Invalid line");
			}
		}

		// setup event
		final Event e = new Event(++eventId, combatLogId, getTimestamp(baseMatcher));

		if (e.getEventId() == 1) {
			// adjust combat log start from the very first event
			combatLog.setTimeFrom(e.getTimestamp());
		}

		// auto detect name if not already (pre v7)
		if (combatLog.getCharacterName() == null
				&& baseMatcher.group("SourcePlayerName") != null && !baseMatcher.group("SourcePlayerName").isEmpty()
				&& baseMatcher.group("SourcePlayerName").equals(baseMatcher.group("TargetPlayerName"))) {
			// found, set
			setCharacterName(getSourceActor(baseMatcher, e.getTimestamp(), null), e.getTimestamp());
		}

		// source
		if (baseMatcher.group("Source") != null && !baseMatcher.group("Source").isEmpty()) {
			e.setSource(getSourceActor(baseMatcher, e.getTimestamp(), baseMatcher.group("EffectGuid")));
		} else {
			e.setSource(context.getActor("Unknown", Actor.Type.NPC));
		}

		// target
		if (baseMatcher.group("Target") != null && !baseMatcher.group("Target").isEmpty()) {
			if ("=".equals(baseMatcher.group("Target"))) {
				e.setTarget(e.getSource());
			} else {
				e.setTarget(getTargetActor(baseMatcher, e.getTimestamp(), baseMatcher.group("EffectGuid")));
			}
		} else {
			if (isEffectiveLogged) {
				e.setTarget(e.getSource());
			} else {
				e.setTarget(context.getActor("Unknown", Actor.Type.NPC));
			}
		}

		if (e.getSource() != null
				&& (Actor.Type.PLAYER.equals(e.getSource().getType()) || Actor.Type.SELF.equals(e.getSource().getType()))) {
			ActorState ac = actorStates.get(e.getSource());
			if (ac == null) {
				ac = new ActorState();
				if (e.getSource().getDiscipline() != null) {
					ac.discipline = e.getSource().getDiscipline(); // v7+
					ac.role = e.getSource().getDiscipline().getRole();
				}
				if (combat != null && (isEffectiveLogged || Actor.Type.SELF.equals(e.getSource().getType()))) {
					context.addCombatPlayer(combat, e.getSource(), ac.discipline);
				}
				actorStates.put(e.getSource(), ac);
				if (Actor.Type.SELF.equals(e.getSource().getType())) {
					//
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Added group player " + e.getSource() + " at " + e.getTs());
					}
				}
			} else if (combat != null) {
				if (isEffectiveLogged || Actor.Type.SELF.equals(e.getSource().getType())) {
					context.addCombatPlayer(combat, e.getSource(), ac.discipline);
				}
			}
		}

		if (zonePattern == null && baseMatcher.group("Value") != null && "836045448945489".equals(baseMatcher.group("EffectGuid"))) { // EnterCombat
			// enter, any ops details? // pre v7
			parseInstanceType(null, null, baseMatcher.group("Value"), null, e.getTimestamp());
		}

		// ability
		if (baseMatcher.group("Ability") != null && !baseMatcher.group("Ability").isEmpty()) {
			e.setAbility(getEntity(
					baseMatcher.group("AbilityName"),
					baseMatcher.group("AbilityGuid")));
		}

		// action
		e.setAction(getEntity(
				baseMatcher.group("ActionName"),
				baseMatcher.group("ActionGuid")));

		// effect
		e.setEffect(getEntity(
				baseMatcher.group("EffectName"),
				baseMatcher.group("EffectGuid")));

		// value (healing / damage)
		if (baseMatcher.group("Value") != null
				&& !EntityGuid.EnterCombat.toString().equals(baseMatcher.group("EffectGuid"))
				&& !EntityGuid.ExitCombat.toString().equals(baseMatcher.group("EffectGuid"))) {
			e.setValue(Integer.parseInt(baseMatcher.group("Value")));
			// critical hit?
			e.setCrit(baseMatcher.group("IsCrit") != null);

			// damage
			if (baseMatcher.group("DamageType") != null && !baseMatcher.group("DamageTypeGuid").equals(EntityGuid.Charges.toString())) {
				e.setDamage(getEntity(
						baseMatcher.group("DamageType"),
						baseMatcher.group("DamageTypeGuid")));
			}

			// reflect
			if (baseMatcher.group("ReflectType") != null) {
				e.setReflect(getEntity(
						baseMatcher.group("ReflectType"),
						baseMatcher.group("ReflectTypeGuid")));
			}

			// mitigation
			if (baseMatcher.group("IsMitigation") != null) {
				if (baseMatcher.group("MitigationType") != null) {
					e.setMitigation(getEntity(
							baseMatcher.group("MitigationType"),
							baseMatcher.group("MitigationTypeGuid")));

					// attack type (incoming attacks to known actors only)
					if (combat != null && actorStates.containsKey(e.getTarget())) {
						processAttackType(e);
					}

				} else {
					// unknown mitigation
					e.setMitigation(getEntity(
							"unknown",
							"-1"));
				}
			}

			// absorption
			if (baseMatcher.group("AbsorbValue") != null) {
				e.setAbsorption(getEntity(
						baseMatcher.group("AbsorbType"),
						baseMatcher.group("AbsorbTypeGuid")));
				e.setAbsorbed(Integer.parseInt(baseMatcher.group("AbsorbValue")));
				if (e.getValue() != null && e.getAbsorbed() != null && e.getAbsorbed() > e.getValue()) {
					e.setAbsorbed(e.getValue());
				}
				if (isEffectiveLogged) {
					final String s;
					if (e.getSource() == e.getTarget()) {
						s = baseMatcher.group("SourceMaxHp"); // no TargetMaxHp for [=]
					} else {
						s = baseMatcher.group("TargetMaxHp");
					}
					final Integer maxHp = s == null ? null : Integer.valueOf(s);
					if (maxHp != null && maxHp < e.getAbsorbed()) {
						// looking at you, Ciphas & Doom
						e.setAbsorbed(maxHp);
					}
				}
			}
		}

		// threat
		if (baseMatcher.group("Threat") != null) {
			e.setThreat(Long.parseLong(baseMatcher.group("Threat")));
		}

		// calculated context values
		// guard
		if (actorStates.containsKey(e.getSource()) || actorStates.containsKey(e.getTarget())) {
			processEventGuard(e);
		}

		// combat
		final boolean wasInCombat = combat != null;
		processEventCombat(e);

		// healing
		if (isEffectHeal(e)) {
			processEventHealing(e, isEffectiveLogged ? baseMatcher.group("Effective") : null);
		}

		// effect
		if (actorStates.containsKey(e.getSource()) || actorStates.containsKey(e.getTarget())) {
			processEventEffect(e);
		}

		// absorption
		processEventAbsorption(e, isEffectiveLogged ? baseMatcher.group("Effective") : null);

		// instance swap?
		if (isEffectEqual(e, EntityGuid.SafeLoginImmunity.getGuid()) && isActionApply(e)) {
			if (zonePattern == null) {
				instanceMode = null;
				instanceSize = null;
			}
			isUsingMimCrystal = false;
			if (logger.isDebugEnabled()) {
				logger.debug("Instance reset at " + e.getTs());
			}

			clearHotsTracking();
//		} else if (isEffectEqual(e, EntityGuid.Bolster.getGuid())) {
//			instanceMode = Raid.Mode.SM;

		} else if (isEffectEqual(e, 3638571739119616L) && isActionApply(e)) { // Nightmare Fury
			instanceMode = Raid.Mode.NiM;
			if (logger.isDebugEnabled()) {
				logger.debug("NiM crystal detected at " + e.getTs());
			}
			if (!isUsingMimCrystal && combat != null) {
				// activated mid fight?
				context.addCombatEvent(combat.getCombatId(), e.getSource(), Event.Type.NIM_CRYSTAL, e.getTimestamp());
			}
			isUsingMimCrystal = true;
		}

		// hots tracking
		if (isSourceThisPlayer(e)
				&& isTargetAnyPlayer(e)
				&& (combat == null
				|| actorStates.get(e.getSource()).discipline == null
				|| CharacterRole.HEALER.equals(actorStates.get(e.getSource()).discipline.getRole()))) {
			processEventHots(e, baseMatcher);
		}

		events.add(e);
		return wasInCombat && combat == null;
	}

	private Long getTimestamp(Matcher m) {

		c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group("HH")));
		c.set(Calendar.MINUTE, Integer.parseInt(m.group("MM")));
		c.set(Calendar.SECOND, Integer.parseInt(m.group("SS")));
		c.set(Calendar.MILLISECOND, Integer.parseInt(m.group("MS")));

		if (lastHour - c.get(Calendar.HOUR_OF_DAY) > 2) {
			// over midnight (2 = ignore daylight saving)
			c.add(Calendar.DAY_OF_MONTH, 1);
		}
		lastHour = c.get(Calendar.HOUR_OF_DAY);

		return c.getTimeInMillis();
	}

	private Actor getSourceActor(final Matcher baseMatcher, final long timestamp, final String effectGuid) {
		return getActor(
				"Source",
				timestamp,
				effectGuid,
				baseMatcher);
	}

	private Actor getTargetActor(final Matcher baseMatcher, final long timestamp, final String effectGuid) {
		return getActor(
				"Target",
				timestamp,
				effectGuid,
				baseMatcher);
	}

	private Actor getActor(final String type, final long timestamp, final String effectGuid, final Matcher baseMatcher) {

		if (baseMatcher.group(type + "CompanionName") != null) {
			return context.getActor(baseMatcher.group(type + "CompanionName"),
					Actor.Type.COMPANION,
					Long.parseLong(baseMatcher.group(type + "CompanionGuid")));
		}

		if (baseMatcher.group(type + "PlayerName") != null) {
			final String playerName = baseMatcher.group(type + "PlayerName");
			return context.getActor(
					playerName,
					playerName.equals(combatLog.getCharacterName()) ? Actor.Type.SELF : Actor.Type.PLAYER);
		}

		if (baseMatcher.group(type + "NpcGuid") != null) {
			// detect raid encounter
			final long guid = Long.parseLong(baseMatcher.group(type + "NpcGuid"));
			if (combat != null && combat.getBoss() == null) {
				combat.setBoss(getRaidBoss(guid, instanceSize, instanceMode));
				if (combat.getBoss() != null) {
					Long eff = effectGuid == null || effectGuid.isEmpty() ? null : Long.valueOf(effectGuid);
					if (eff != null && (eff.equals(EntityGuid.TargetSet.getGuid()) || eff.equals(EntityGuid.TargetCleared.getGuid()))) {
						// ignore clicking on boss
						combat.setBoss(null);
					}
				}
				if (combat.getBoss() != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Boss detected as [" + combat.getBoss() + "] at " + Event.formatTs(timestamp));
						if (Boolean.TRUE.equals(combat.isPvp())) {
							logger.debug("Removed previously set PvP flag at " + Event.formatTs(timestamp));
						}
					}
					combat.setIsPvp(false); // explicitly set to false (as it may be both, e.g. open world PvE grieving)

					// upgrades available?
					combatBossUpgrade = isEffectiveLogged ? null /* already set */ : combat.getBoss().getPossibleUpgrade();
					if (combatBossUpgrade != null && (instanceMode != null || instanceSize != null)) {
						// try to use the already-confident mode and size for this instance
						resolveCombatUpgrade(combatBossUpgrade.upgradeByModeAndSize(instanceMode, instanceSize), true);
					}
					if (combatBossUpgrade == null) {
						instanceMode = combat.getBoss().getMode();
						instanceSize = combat.getBoss().getSize();
					}
				}
			}
			if (combat != null && combatBossUpgrade != null) {
				resolveCombatUpgrade(combatBossUpgrade.upgradeByNpc(guid), true);
			}
			final Actor actor = context.getActor(
					baseMatcher.group(type + "NpcName"),
					Actor.Type.NPC,
					guid,
					baseMatcher.group(type + "NpcInstance") != null ? Long.parseLong(baseMatcher.group(type + "NpcInstance")) : 0L,
					isEffectiveLogged ? baseMatcher.group(type + "X") : null,
					isEffectiveLogged ? baseMatcher.group(type + "Y") : null,
					isEffectiveLogged ? baseMatcher.group(type + "Angle") : null
			);

			if (combat != null && combat.getBoss() != null && combat.getBoss().getRaid().getNpcs().containsKey(guid)) {
				context.setCombatActorState(combat,
						actor,
						combat.getBoss().getRaid().getNpcs().get(guid),
						baseMatcher.group(type + "CurrentHp"),
						baseMatcher.group(type + "MaxHp"),
						timestamp - combat.getTimeFrom());
			}

			return actor;
		}

		throw new IllegalArgumentException("Invalid actor:"
				+ " npcName[" + baseMatcher.group(type + "NpcName") + "]"
				+ " npcGuid[" + baseMatcher.group(type + "NpcGuid") + "] at " + Event.formatTs(timestamp));
	}

	private Entity getEntity(String name, String guid) {
		return context.getEntity(name, Long.parseLong(guid));
	}

	private void parseInstanceType(
			final String instanceName,
			final String instanceGuid,
			final String instanceType,
			final String instanceTypeGuid,
			final long timestamp) {
		Raid.Size s = null;
		Raid.Mode m = null;
		if ("836045448953652".equals(instanceTypeGuid)) {
			s = Size.Eight;
			m = Mode.HM;
		} else if ("836045448953651".equals(instanceTypeGuid)) {
			s = Size.Eight;
			m = Mode.SM;
		} else if ("836045448953655".equals(instanceTypeGuid)) {
			s = Size.Eight;
			m = Mode.NiM;
		} else if (instanceType != null) {
			if (instanceType.contains("16 ") || instanceType.contains("16 ")) {
				s = Size.Sixteen;
			} else if (instanceType.contains("8 ") || instanceType.contains("8 ")) {
				s = Size.Eight;
			}
			if (instanceType.contains("Story") || instanceType.contains("histoire")) {
				m = Mode.SM;
			} else if (instanceType.contains("Veteran") || instanceType.contains("vétéran")) {
				m = Mode.HM;
			} else if (instanceType.contains("Master") || instanceType.contains("maître")) {
				m = Mode.NiM;
			}
		}
		if (instanceName != null) {
			this.instanceName = instanceName;
		}
		if (instanceGuid != null) {
			this.instanceGuid = Long.valueOf(instanceGuid);
		}
		if (s != null && m != null) {
			instanceMode = m;
			instanceSize = s;
			if (logger.isDebugEnabled()) {
				logger.debug("Instance set as " + s + " " + m + " (" + instanceName + " " + instanceGuid + ") at " + Event.formatTs(timestamp));
			}
			context.setLocationInfo(new LocationInfo(instanceMode, instanceSize, instanceName, this.instanceGuid));
		}
	}

	public void processEventGuard(final Event e) throws ParserException {

		if (isEffectGuard(e) && (actorStates.containsKey(e.getSource()) || actorStates.containsKey(e.getTarget()))) {
			if (isActionApply(e)) {
				// guard gained
				if (!actorStates.containsKey(e.getTarget())) {
					actorStates.put(e.getTarget(), new ActorState());
				}

				actorStates.get(e.getTarget()).guarded = (actorStates.get(e.getTarget()).guarded < 2
						? (actorStates.get(e.getTarget()).guarded + 1)
						: 2);

			} else if (isActionRemove(e)) {
				// guard lost
				if (actorStates.containsKey(e.getTarget())) {
					actorStates.get(e.getTarget()).guarded = (actorStates.get(e.getTarget()).guarded > 0
							? (actorStates.get(e.getTarget()).guarded - 1)
							: 0);
				}

			} else {
				throw new ParserException("Unknown guard action: " + e);
			}
		}

		// store context for current player always (TODO: useless?)
		if (isSourceThisPlayer(e)) {
			e.setGuardState(actorStates.containsKey(e.getSource()) ? actorStates.get(e.getSource()).guarded : 0);
		} else/* target */ {
			e.setGuardState(actorStates.containsKey(e.getTarget()) ? actorStates.get(e.getTarget()).guarded : 0);
		}
	}

	public void processEventCombat(final Event e) {

		// resolve combat
		if (combatConnectLimit != null && combatConnectLimit < e.getTimestamp()) {
			// window expired, close the running combat for good
			closeCurrentCombat();
		}

		final ActorState ac = actorStates.get(e.getSource());
		if (isEffectEnterCombat(e) && !isSourceOtherPlayer(e) /* safety vor v7+ */) {
			if (combatConnectLimit != null) {
				// within limit, reopen last combat
				combat.setEventIdTo(null);
				combat.setTimeTo(null);
				combatConnectLimit = null;

			} else if (combat != null) {
				// sometimes new combat is created even without exiting the previous one
				if (logger.isDebugEnabled()) {
					logger.debug("New combat was entered without exiting the previous one, silently connecting");
				}

			} else {
				// setup new combat
				combat = new Combat(++combatId, combatLogId, e.getTimestamp(), e.getEventId());
				context.getCombatInfo().put(combat.getCombatId(), new CombatInfo(new LocationInfo(instanceMode, instanceSize, instanceName, instanceGuid)));
				for (final Actor a : actorStates.keySet()) {
					actorStates.get(a).role = null; // might have re-specced
					actorStates.get(a).discipline = null;
				}
				if (isUsingMimCrystal) {
					context.addCombatEvent(combat.getCombatId(), e.getSource(), Event.Type.NIM_CRYSTAL, e.getTimestamp());
				}

				// assemble combat players; make sure SELF is there
				for (Map.Entry<Actor, ActorState> entry : actorStates.entrySet()) {
					entry.getValue().combatTotalThreat = 0;
					if (Actor.Type.SELF.equals(entry.getKey().getType())) {
						context.addCombatPlayer(combat, entry.getKey(), entry.getKey().getDiscipline());
						break;
					}
				}
			}

		} else if (combat != null && combatConnectLimit == null
				&& (isEffectExitCombat(e) || (isTargetThisPlayer(e) && isEffectDeath(e)) || isEffectLoginImmunity(e))) {
			// exit event detected (and no window is set yet), setup candidates
			combat.setEventIdTo(e.getEventId());
			combat.setTimeTo(e.getTimestamp());

			if (isEffectExitCombat(e)) {
				context.addCombatEvent(combat.getCombatId(), e.getSource(), Event.Type.COMBAT_EXIT, e.getTimestamp());
			}

			// ... and setup limit for either revive or quick reconnect (and for lagged damage and healing)
			combatConnectLimit = e.getTimestamp() + (isEffectDeath(e)
					// died - setup REVIVE limit
					? COMBAT_REVIVE_WINDOW
					: ((lastCombatDropEvent != null && (lastCombatDropEvent.getTimestamp() > (e.getTimestamp() - COMBAT_RETURN_WINDOW))
					// recent (yet already consumed) combat drop event - "drop->enter->exit[now]->?enter" sequence can happen - setup RETURN window
					? COMBAT_RETURN_WINDOW
					// just regular window for delayed damage/healing events
					: COMBAT_DELAY_WINDOW)));

		} else if (combat != null && combatConnectLimit != null) {
			if (isTargetThisPlayer(e) && isEffectRevive(e)) {
				// revived within REVIVE limit, setup RETURN limit and keep waiting
				combatConnectLimit = e.getTimestamp() + COMBAT_RETURN_WINDOW;

			} else if (isTargetThisPlayer(e) && isEffectCombatDrop(e)) {
				// combat drop detected within DELAY window, setup RETURN limit and keep waiting
				combatConnectLimit = e.getTimestamp() + COMBAT_RETURN_WINDOW;
				// all combat drops are suspicious as you can drop, enter, kill/discard add, exit combat and then enter again (e.g. Raptus challenge)
				lastCombatDropEvent = e;

			} else if (isSourceThisPlayer(e) && e.getValue() != null && (e.getThreat() != null || isEffectDamage(e))) {
				// gracefully include any delayed damage/healing abilities
				// (after dying, DOTs can be still ticking, although causing no threat)
				combat.setEventIdTo(e.getEventId());
				// combat.setTimeTo(e.getTimestamp()); // do not extend, keep the original time
			}
		}

		if (combat != null && isEffectDeath(e)) {
			context.addCombatEvent(combat.getCombatId(), e.getTarget(), Event.Type.DEATH, e.getTimestamp());
		}

		// resolve effective threat
		if (combat != null && ac != null && e.getThreat() != null) {
			if (ac.combatTotalThreat + e.getThreat() < 0) {
				e.setEffectiveThreat(ac.combatTotalThreat * -1);
				ac.combatTotalThreat = 0;
			} else {
				e.setEffectiveThreat(e.getThreat());
				ac.combatTotalThreat += e.getThreat();
			}
		}

		// resolve combat phase (ignore target set/cleared to avoid confusion)
		if (combat != null && combat.getBoss() != null
				&& !(e.getEffect() != null && (e.getEffect().getGuid().equals(EntityGuid.TargetSet.getGuid()) || e.getEffect().getGuid().equals(EntityGuid.TargetCleared.getGuid())))
		) {
			final String newBossPhaseName;
			if ((newBossPhaseName = combat.getBoss().getRaid().getNewPhaseName(e, combat,
					currentBossPhase != null ? currentBossPhase.getName() : null)) != null) {
				// new phase detected
				if (currentBossPhase != null) {
					// close old - bounds are explicitly as <from, to)
					closePhase(currentBossPhase, e.getEventId() - 1, e.getTimestamp() - combat.getTimeFrom() - 1);
				}
				// setup new (if this is the very first one, assume it started at the beginning of this combat)
				currentBossPhase = new Phase(++phaseId, newBossPhaseName, Phase.Type.BOSS,
						combat.getCombatId(),
						(currentBossPhase == null ? combat.getEventIdFrom() : e.getEventId()),
						(currentBossPhase == null ? 0 : e.getTimestamp() - combat.getTimeFrom()));
			}

			if (combatBossUpgrade != null && e.getAbility() != null && e.getAbility().getGuid() != null) {
				resolveCombatUpgrade(combatBossUpgrade.upgradeByAbility(e.getAbility().getGuid(), e.getEffect().getGuid(), e.getValue()), false);
			}
		}

		// resolve damage phase
		if (combat != null && isSourceThisPlayer(e) && isEffectDamage(e) && e.getValue() > 0) {
			if (firstDamageEvent == null) {
				// open new damage phase
				firstDamageEvent = e;

			} else if (lastDamageEvent != null && (lastDamageEvent.getTimestamp() + PHASE_DAMAGE_WINDOW < e.getTimestamp())) {
				// close damage phase and open new one
				createDamagePhase(firstDamageEvent, lastDamageEvent);
				lastDamageEvent = null;
				firstDamageEvent = e;

			} else {
				// prolong current damage phase
				lastDamageEvent = e;
			}
		}

		// resolve discipline
		if (combat != null && ac != null && ac.discipline == null) {
			if (!ac.isDualWield && isEffectDualWield(e)) {
				ac.isDualWield = true;
			}
			if (isEffectAbilityActivate(e)) {
				ac.discipline = getDiscipline(e.getAbility().getGuid(), ac.isDualWield);
				if (ac.discipline != null) {
					ac.role = ac.discipline.getRole();
					if (logger.isDebugEnabled()) {
						logger.debug(e.getSource() + ": Discipline detected as [" + ac.discipline + "] at " + e.getTs());
					}
					if (isEffectiveLogged || Actor.Type.SELF.equals(e.getSource().getType())) {
						context.addCombatPlayer(combat, e.getSource(), ac.discipline);
					}
					if (isSourceThisPlayer(e)) {
						// self
						if (!CharacterRole.HEALER.equals(ac.discipline.getRole())) {
							// make sure its reset
							clearHotsTracking();
						}
					}
				}
			}
		}
	}

	private void closeCurrentCombat() {
		combatConnectLimit = null;
		combats.add(combat);

		if (currentBossPhase != null && combat.getEventIdTo() != null) {
			// close running phase - bounds explicitly set as <from, to>
			closePhase(currentBossPhase, combat.getEventIdTo(), ( // extend after the combat boundary if there was a delayed damage event
					lastDamageEvent != null && lastDamageEvent.getTimestamp() > combat.getTimeTo()
							? lastDamageEvent.getTimestamp()
							: combat.getTimeTo())
					- combat.getTimeFrom());
			currentBossPhase = null;
		}
		if (lastDamageEvent != null) {
			createDamagePhase(firstDamageEvent, lastDamageEvent);
		}
		firstDamageEvent = lastDamageEvent = lastCombatDropEvent = null;

		combat = null;
		combatBossUpgrade = null;

		TimerManager.stopAllTimers(BaseTimer.Scope.COMBAT);
	}

	private void createDamagePhase(final Event eventFrom, final Event eventTo) {
		if ((eventTo.getTimestamp() - eventFrom.getTimestamp()) < PHASE_DAMAGE_MININUM) {
			// too short, ignore
			return;
		}
		closePhase(new Phase(++phaseId, "Damage", Phase.Type.DAMAGE,
						combat.getCombatId(),
						eventFrom.getEventId(),
						eventFrom.getTimestamp() - combat.getTimeFrom()),
				eventTo.getEventId(), eventTo.getTimestamp() - combat.getTimeFrom());
	}

	private void closePhase(final Phase phase, int eventIdTo, long tickTo) {
		phase.setEventIdTo(eventIdTo);
		phase.setTickTo(tickTo);
		phases.add(phase);
	}

	public void processEventHealing(final Event e, final String effectiveValue) {

		if (e.getThreat() != null) {
			if (isAbilityFakeHeal(e)) {
				// ignore
				return;
			}
			// continue normally

		} else if (!isEffectiveLogged) {
			if (isAbilityNoThreat(e) && e.getSource() == e.getTarget()) {
				// consider as fully effective
				e.setEffectiveHeal(e.getValue());

			} else {
				// no effective, nothing to do
			}
			return;
		}

		// resolve effective healing
		if (!actorStates.containsKey(e.getSource())) {
			// setup healer
			actorStates.put(e.getSource(), new ActorState());
		}

		// detect healer if possible
		if (actorStates.get(e.getSource()).role == null) {
			final CharacterDiscipline actorDiscipline = getDiscipline(e.getAbility().getGuid(), false);
			if (actorDiscipline != null) {
				actorStates.get(e.getSource()).role = actorDiscipline.getRole();
//				if (logger.isDebugEnabled()) {
//					logger.debug("Healing threat ratio set to " + actorDiscipline.getRole() + " for " + e.getSource() + " at " + e.getTs());
//				}
			}
		}

		if (isEffectiveLogged) {
			if (effectiveValue != null && !effectiveValue.isEmpty()) {
				e.setEffectiveHeal("0".equals(effectiveValue) || effectiveValue.startsWith("-") /* FIXME */ ? null : Integer.valueOf(effectiveValue));
			} else {
				e.setEffectiveHeal(e.getValue());
			}
			return;
		}

		// shortcuts
		final boolean isGuarded = actorStates.get(e.getSource()).guarded > 0;
		final boolean isHealer = actorStates.get(e.getSource()).role == null || CharacterRole.HEALER.equals(actorStates.get(e.getSource()).role);
		final boolean isTank = !isHealer && CharacterRole.TANK.equals(actorStates.get(e.getSource()).role);

		// calculate effective heal using the threat generated
		int effectiveHeal = getEffectiveHeal(e.getThreat(),
				isHealer && !isAbilityNonreducedThreat(e),
				isGuarded,
				isTank);

		// sanity check
		if (effectiveHeal == e.getValue() || Math.abs(effectiveHeal - e.getValue()) <= HEALING_THREAT_TOLERANCE) {
			// fully effective (possibly with rounding issue)
			e.setEffectiveHeal(e.getValue());
			return;
		}

		if (effectiveHeal < e.getValue()) {
			// not fully effective
			e.setEffectiveHeal(effectiveHeal);

			if (!isGuarded) {
				// try with guard on
				effectiveHeal = getEffectiveHeal(e.getThreat(),
						isHealer && !isAbilityNonreducedThreat(e),
						true,
						isTank);
				if (Math.abs(effectiveHeal - e.getValue()) <= HEALING_THREAT_TOLERANCE) {
					// target is guarded, fix the flag
//					if (logger.isDebugEnabled()) {
//						logger.debug("Healing threat is reduced by guard, fixed for " + e.getSource() + " at " + e.getTs());
//					}
					actorStates.get(e.getSource()).guarded = 1;

					e.setEffectiveHeal(e.getValue());
					return;
				}
			}

			// nothing to be done (e.g. unable to detect not fully effective heals from guarded healers)
			return;
		}

		// value is too high - the ratios are off, try to detect the error ...
		if (isHealer && !isAbilityNonreducedThreat(e)) {
			// try without the 10% healing reduction
			effectiveHeal = getEffectiveHeal(e.getThreat(),
					false,
					isGuarded,
					false);

			if (effectiveHeal < (e.getValue() + HEALING_THREAT_TOLERANCE)) {
				if (CharacterRole.HEALER.equals(actorStates.get(e.getSource()).role)) {
					// we know for sure this is a healer, therefore 10% reduced, so assume
					// it is actually the ability not being affected by it (true for many HOTs etc)

				} else {
					// healing threat reduction not active (probably a DPS class off-healing), reset
					actorStates.get(e.getSource()).role = CharacterRole.DPS;
//					if (logger.isDebugEnabled()) {
//						logger.debug("Healing threat reduction removed for " + e.getSource() + " at " + e.getTs());
//					}
				}

				e.setEffectiveHeal(Math.min(e.getValue(), effectiveHeal));
				return;
			}
		}

		if (isGuarded) {
			// try without the guard 25% reduction
			effectiveHeal = getEffectiveHeal(e.getThreat(),
					isHealer && !isAbilityNonreducedThreat(e),
					false,
					isTank);
			if (effectiveHeal <= (e.getValue() + HEALING_THREAT_TOLERANCE)) {
				// supposedly guarded, but the threat is not reduced (possibly SWTOR bug) - reset the flag
				actorStates.get(e.getSource()).guarded = 0;
//				if (logger.isDebugEnabled()) {
//					logger.debug("Healing threat is not reduced by guard, cancelled for " + e.getSource() + " at " + e.getTs());
//				}

				e.setEffectiveHeal(Math.min(e.getValue(), effectiveHeal));
				return;
			}
		}

		if (isGuarded && isHealer && !isAbilityNonreducedThreat(e)) {
			// both of the above combined (without 35% reduction)
			effectiveHeal = getEffectiveHeal(e.getThreat(),
					false,
					false,
					false);
			if (effectiveHeal <= (e.getValue() + HEALING_THREAT_TOLERANCE)) {
				actorStates.get(e.getSource()).guarded = 0;
				if (logger.isDebugEnabled()) {
					logger.debug("Healing threat is not reduced by guard, cancelled for " + e.getSource() + " at " + e.getTs());
				}

				if (CharacterRole.HEALER.equals(actorStates.get(e.getSource()).role)) {
					// we know for sure this is a healer, therefore 10% reduced, so assume
					// it is actually the ability not being affected by it (true for many HOTs etc)

				} else {
					actorStates.get(e.getSource()).role = CharacterRole.DPS;
//					if (logger.isDebugEnabled()) {
//						logger.debug("Healing threat reduction removed for " + e.getSource() + " at " + e.getTs());
//					}
				}

				e.setEffectiveHeal(Math.min(e.getValue(), effectiveHeal));
				return;
			}
		}

		if (!isTank) {
			// try with tanking 200% ratio (and without the reduction if any)
			effectiveHeal = getEffectiveHeal(e.getThreat(),
					false,
					isGuarded,
					true);

			if (effectiveHeal <= (e.getValue() + HEALING_THREAT_TOLERANCE)) {
				actorStates.get(e.getSource()).role = CharacterRole.TANK;
				if (logger.isDebugEnabled()) {
					logger.debug("Healing threat ratio set to tank for " + e.getSource() + " at " + e.getTs());
				}

				e.setEffectiveHeal(Math.min(e.getValue(), effectiveHeal));
				return;
			}
		}

		if (!isTank && isGuarded) {
			// try again, this time even without a guard
			effectiveHeal = getEffectiveHeal(e.getThreat(),
					false,
					false,
					true);

			if (effectiveHeal <= (e.getValue() + HEALING_THREAT_TOLERANCE)) {
				actorStates.get(e.getSource()).guarded = 0;
				if (logger.isDebugEnabled()) {
					logger.debug("Healing threat is not reduced by guard, cancelled for " + e.getSource() + " at " + e.getTs());
				}

				actorStates.get(e.getSource()).role = CharacterRole.TANK;
				if (logger.isDebugEnabled()) {
					logger.debug("Healing threat ratio set to tank for " + e.getSource() + " at " + e.getTs());
				}

				e.setEffectiveHeal(Math.min(e.getValue(), effectiveHeal));
				return;
			}
		}

		e.setEffectiveHeal(Math.min(e.getValue(), effectiveHeal));
		logger.warn("Unknown heal ratio for event: " + e);
		//throw new ParserException("Unknown heal ratio for event: " + e);
	}

	public int getEffectiveHeal(long threat, boolean isReduced, boolean isGuarded, boolean isTank) {
		// default with guard: (0.5*0.75)-(0.5*0.1)
		return (int) Math.ceil(threat
				/ THREAT_HEAL
				/ (isTank ? THREAT_TANK : 1)
				/ ((isGuarded ? THREAT_GUARD : 1) - (isReduced ? THREAT_HEAL_REDUCTION : 0)));
	}

	public void processEventEffect(final Event e) {

		if (isEffectAbilityActivate(e) && !activatedAbilities.contains(e.getAbility().getGuid())) {
			activatedAbilities.add(e.getAbility().getGuid());
		}

		// effects
		if (isEffectHeal(e) || isEffectDamage(e)) {

			if (isSourceThisPlayer(e)) {
				for (int i = currentEffects.size() - 1; i > 0; i--) {
					if (currentEffects.get(i).getEffect().equals(e.getAbility()) && currentEffects.get(i).getTarget().equals(e.getTarget())) {
						// effect causing ticks on target, prolong by this event
						currentEffects.get(i).setEventIdTo(e.getEventId());
						currentEffects.get(i).setTimeTo(e.getTimestamp());
						break;
					}
				}
			}

			if (combat != null && Boolean.TRUE.equals(combat.isPvp())) {
				if (e.getSource().isHostile() == null && isSourceOtherPlayer(e) && (isTargetThisPlayer(e) || Boolean.FALSE.equals(e.getTarget().isHostile()))) {
					e.getSource().setIsHostile(isEffectDamage(e));
				}
				if (e.getTarget().isHostile() == null && isTargetOtherPlayer(e) && (isSourceThisPlayer(e) || Boolean.FALSE.equals(e.getSource().isHostile()))) {
					e.getTarget().setIsHostile(isEffectDamage(e));
				}
			}

		} else if ((isActionApply(e) || isActionRemove(e)) && !isEffectGeneric(e) && !isEffectGuard(e) && !isAbilityGeneric(e)) {
			// setup effect key as "source @ target: effect (ability)"
			EffectKey effectKey = new EffectKey(e.getSource(), e.getTarget(), e.getEffect(), e.getAbility());

			// already running?
			List<Effect> effectInstances = runningEffects.get(effectKey);

			if (isActionApply(e)) {

				if (combat != null && combat.isPvp() == null && isEffectPvP(e)) {
					// flag as PVP
					combat.setIsPvp(true);
					if (logger.isDebugEnabled()) {
						logger.debug("PvP detected for " + combat.getCombatId() + " at " + e.getTs());
					}
				}

				if (effectInstances != null && (e.getTimestamp() - effectInstances.get(0).getTimeFrom() <= EFFECT_OVERLAP_TOLERANCE)) {
					// another start (buff) of the same effect within tolerance, just count it as another "stack", without actually creating new
					// instance
					// examples: Commando's Electro Net, Brontes' Static Field, etc
					stackedEffects.put(effectKey, (stackedEffects.containsKey(effectKey) ? stackedEffects.get(effectKey) + 1 : 1));
					return;
				}

				// start new effect
				final boolean isAbsorption = isEffectAbsorption(e);
				final Effect newEffect = new Effect(++effectId,
						e.getEventId(), e.getTimestamp(),
						e.getSource(), e.getTarget(),
						e.getAbility(), e.getEffect(),
						activatedAbilities.contains(e.getEffect().getGuid()),
						isAbsorption);

				if (effectInstances != null) {
					// another occurrence of known effect (debuff, overlapping etc)
					// (disallow multiple instances of absorption effects)
					while (effectInstances.size() > (isAbsorption ? 0 : 1)) {
						// more than two beginnings without any finish, clean-up
						// logger.debug("Discarding multiple effect: "+effectStack.get(0)+" at "+e);
						closeEffect(effectInstances.remove(0), null);
					}

				} else {
					// brand new
					runningEffects.put(effectKey, new ArrayList<>());
					effectInstances = runningEffects.get(effectKey);

					if (isEffectiveLogged && e.getValue() != null) {
						chargedEffects.put(newEffect, e.getValue());
					}
				}

				currentEffects.add(newEffect);
				effectInstances.add(newEffect);

				if (isAbsorption) {

					if (absorptionEffectsConsumed.get(e.getTarget()) != null) {
						// new absorption effect = always discard all candidates
						while (absorptionEffectsConsumed.get(e.getTarget()).size() > 0) {
							final Effect removed = absorptionEffectsConsumed.get(e.getTarget()).remove(0);
							absorptionEffectsRunning.computeIfPresent(e.getTarget(), (t, l) -> {
								l.remove(removed);
								return l;
							});
						}
					}
					// new absorption effect, append to the stack
					absorptionEffectsRunning.computeIfAbsent(e.getTarget(), (t) -> new ArrayList<>()).add(newEffect);
				}

			} else {
				// finishing running effect

				if (effectInstances == null && e.getSource().getName().equals("Unknown")) {
					for (EffectKey k : runningEffects.keySet()) {
						if (k.getEffect().equals(e.getEffect()) && k.getTarget().equals(e.getTarget())) {
							// effect is being removed from a "missing" source, guess it by its type and target
							effectKey = k;
							effectInstances = runningEffects.get(effectKey);
							break;
						}
					}
				}

				if (stackedEffects.containsKey(effectKey)) {
					// just decrement the stack (buff) count and hope there is another event which will actually remove it
					if (stackedEffects.get(effectKey) == 1) {
						stackedEffects.remove(effectKey);
					} else {
						stackedEffects.put(effectKey, stackedEffects.get(effectKey) - 1);
					}
					return;
				}

				if (effectInstances != null) {

					if (effectInstances.size() > 1) {
						// multiple starting events found (start A ... start B ... [end A or B?])

						if (e.getTimestamp() - (effectInstances.get(effectInstances.size() - 1)).getTimeFrom() <= EFFECT_OVERLAP_TOLERANCE) {
							// allow overlapping events if within limit (start A ... start B ... [end A])

						} else {
							// not overlapping, discard (debuffs, death etc)
							closeEffect(effectInstances.remove(0), null);
						}
					}

					// close effect by this event
					closeEffect(effectInstances.remove(0), e);

					if (effectInstances.size() == 0) {
						// all occurrences finished
						runningEffects.remove(effectKey);
					}

				} else {
					final boolean isAbsorption = isEffectAbsorption(e);
					if (isAbsorption) {
						final Effect newEffect = new Effect(++effectId,
								e.getEventId(), e.getTimestamp(),
								e.getSource(), e.getTarget(),
								e.getAbility(), e.getEffect(),
								activatedAbilities.contains(e.getEffect().getGuid()),
								true);
						absorptionEffectsRunning.computeIfAbsent(e.getTarget(), (t) -> new ArrayList<>()).add(newEffect);
						closeEffect(newEffect, e);
					}

					if (logger.isDebugEnabled() && isTargetThisPlayer(e)) {
						// missing "beginning", can happen, just ignore
						// for 7+, missing beginnings/ends are common due to actors randomly appearing and disappearing (re: range)
						logger.debug("Missing beginning: " + effectKey + " at " + e.getTs());
					}
				}
			}
		}
	}

	private void closeEffect(final Effect effect, final Event event) {

		if (event != null) {
			effect.setEventIdTo(event.getEventId());
			effect.setTimeTo(event.getTimestamp());
		}

		if (!effect.isActivated() && activatedAbilities.contains(effect.getEffect().getGuid())) {
			// activation ability picked up later
			if (logger.isDebugEnabled()) {
				logger.debug("Activating effect: " + effect);
			}
			effect.setIsActivated(true);
		}

		final List<Effect> absRunning = absorptionEffectsRunning.get(effect.getTarget());
		if (absRunning != null && absRunning.contains(effect)) {
			absRunning.remove(effect);
			absorptionEffectsConsumed.computeIfPresent(effect.getTarget(), (t, l) -> {
				l.remove(effect);
				return l;
			});

			if (effect.getTimeTo() != null) {
				absorptionEffectsClosing.computeIfAbsent(effect.getTarget(), (t) -> new ArrayList<>()).add(effect);
				if (!isEffectiveLogged && absorptionEffectsConsumed.containsKey(effect.getTarget())) {
					while (absorptionEffectsConsumed.get(effect.getTarget()).size() > 0) {
						absRunning.remove(absorptionEffectsConsumed.get(effect.getTarget()).remove(0));
					}
				}
			}
		}

		// move
		currentEffects.remove(effect);
		chargedEffects.remove(effect);
		effects.add(effect);
	}

	public void processEventAbsorption(final Event e, final String effectiveValue) {

		// is this an absorption event to be linked?
		boolean isEventUnlinked = e.getAbsorbed() != null && isTargetAnyPlayer(e);
		final Integer effective = effectiveValue == null || effectiveValue.isEmpty() ? null : Integer.parseInt(effectiveValue);
		if (isEventUnlinked && isEffectiveLogged && (effective == null
				|| Math.abs(effective - e.getValue()) <= 1
				|| ((effective + e.getAbsorbed() - e.getValue()) > 100))
		) {
			if ((e.getTarget().getDiscipline() == null || (e.getTarget().getDiscipline() != null
					&& e.getTarget().getDiscipline().getRole().equals(CharacterRole.TANK)))
					&& e.getMitigation() != null
					&& EntityGuid.Shield.getGuid() == e.getMitigation().getGuid()) {
				isEventUnlinked = false; // ignore this, tank shield broken logging
				e.setAbsorbed(null);
			}
		}

		final Actor a = e.getTarget();
		if (absorptionEffectsClosing.containsKey(e.getTarget()) && !absorptionEffectsClosing.get(a).isEmpty()) {
			final List<Effect> absClosing = absorptionEffectsClosing.get(a);
			final Iterator<Effect> absClosingIt = absClosing.listIterator();
			// resolve already closed effects (in the order of their end)
			while (absClosingIt.hasNext()) {
				final Effect effect = absClosingIt.next();

				if (absorptionEventsInside.containsKey(a)) {
					// link pending INSIDE events to this effects as it was the first one ending
					linkAbsorptionEvents(absorptionEventsInside.get(a), effect);
					absorptionEventsInside.remove(a);
				}

				if (effect.getTimeTo() < e.getTimestamp() - ((absorptionEffectsRunning.containsKey(a) && !absorptionEffectsRunning.get(a).isEmpty())
						// resolve possible delay (long if this is the only effect, short if another is available as well)
						? ABSORPTION_INSIDE_DELAY_WINDOW
						: ABSORPTION_OUTSIDE_DELAY_WINDOW)) {
					// effect is expiring

					if (absorptionEventsOutside.containsKey(a) && absClosing.size() == 1) {
						// link pending OUTSIDE events to this effect as it is the only choice (should be only 1 anyway)
						linkAbsorptionEvents(absorptionEventsOutside.get(a), effect);
						absorptionEventsOutside.remove(a);
					}

					// effect was closed and expired, remove it for good
					absClosingIt.remove();
					absorptionEffectsJustClosed.add(effect);
					continue;

				} else if (!isEventUnlinked) {
					// not expired yet and this is not an absorption event, nothing else to do
					continue;
				}

				// we are here = this event is an absorption & this closing effect is within a delay window
				if (absorptionEventsOutside.containsKey(a)) {
					// link queued OUTSIDE events to this effect (since this is another absorption, so there is nothing to wait for)
					linkAbsorptionEvents(absorptionEventsOutside.get(a), effect);
					absorptionEventsOutside.remove(a);

					if (e.getAbsorbed() + 1 < e.getValue()) {
						// ... and consume the effect as there are another active
						// (unless the mitigation was full - high chance the remaining charge will be used in a bit)
						absClosingIt.remove();
						absorptionEffectsJustClosed.add(effect);
						continue;
					}
				}

				// try to link this absorption event
				if (absClosing.size() > 1) {
					// not clear to which effect to link to, queue as OUTSIDE and wait
					absorptionEventsOutside.computeIfAbsent(a, (t) -> new ArrayList<>()).add(e.getEventId());

				} else {
					// link to the just closed effect
					absorptions.add(new Absorption(e.getEventId(), effect.getEffectId()));

					if (e.getAbsorbed() + 1 < e.getValue()) {
						// ... and consume the effect as there are another active
						// (unless the mitigation was full - high chance the remaining charge will be used in a bit)
						absClosingIt.remove();
						absorptionEffectsJustClosed.add(effect);
					}
				}

				if (a == e.getTarget()) {
					// flag event as linked
					isEventUnlinked = false;
				}
			}
			if (absClosing.isEmpty()) {
				absorptionEffectsClosing.remove(a);
			}
		}

		if (!isEventUnlinked) {
			// event was already linked to a closed effect or its not an absorption at all
			return;
		}

		final List<Effect> absRunning = absorptionEffectsRunning.get(e.getTarget());
		if (absRunning == null || absRunning.isEmpty()) {
			if (isEffectiveLogged) {
				absorptionEffectsJustClosed.removeIf((eff) -> eff.getTimeTo() < e.getTimestamp() - ABSORPTION_OUTSIDE_DELAY_WINDOW);
				final Optional<Effect> lagged = absorptionEffectsJustClosed.stream().filter((eff) -> eff.getTarget() == e.getTarget()).findFirst();
				if (lagged.isPresent()) {
					absorptions.add(new Absorption(e.getEventId(), lagged.get().getEffectId()));
					return;
				}
			}

			// no absorption effect currently active
			if (logger.isDebugEnabled()) {
				logger.error("Unknown absorption: " + e);
			}

		} else if (absRunning.size() == 1) {
			// exactly one absorption effect active, link it
			absorptions.add(new Absorption(e.getEventId(), absRunning.get(0).getEffectId()));
			// try to be smart - if the mitigation was not full, then its probably consumed
//			if (e.getAbsorbed() + 1 < e.getValue() && !chargedEffects.containsKey(absRunning.get(0))) {
//				absorptionEffectsConsumed.computeIfAbsent(e.getTarget(), (t) -> new ArrayList<>()).add(absRunning.get(0));
//			}

		} else {
			// try to be smart - if the mitigation was not full, then its probably consumed by the first one activated
//			if (e.getAbsorbed() + 1 < e.getValue() && !chargedEffects.containsKey(absRunning.get(0))) {
//				absorptions.add(new Absorption(e.getEventId(), absRunning.get(0).getEffectId()));
//				absorptionEffectsConsumed.computeIfAbsent(e.getTarget(), (t) -> new ArrayList<>()).add(absRunning.get(0));
//			} else {
			// multiple absorption effects currently active, queue as INSIDE and wait whichever finishes first ...
			absorptionEventsInside.computeIfAbsent(e.getTarget(), (t) -> new ArrayList<>()).add(e.getEventId());
//			}
		}
	}

	private void linkAbsorptionEvents(final List<Integer> events, final Effect effect) {
		if (events == null) {
			return;
		}
		for (Integer pendingEventId : events) {
			absorptions.add(new Absorption(pendingEventId, effect.getEffectId()));
		}
		events.clear();
	}

	public void processAttackType(final Event e) {

		if (e.getAbility() == null || e.getAbility().getGuid() == null) {
			return;
		}

		if (e.getMitigation() == null || e.getMitigation().getGuid() == null) {
			return;
		}

		if (context.getAttacks().containsKey(e.getAbility().getGuid())) {
			// already classified
			return;
		}

		if (EntityGuid.Parry.getGuid() == e.getMitigation().getGuid()
				|| EntityGuid.Deflect.getGuid() == e.getMitigation().getGuid()
				|| EntityGuid.Dodge.getGuid() == e.getMitigation().getGuid()) {
			context.addAttack(AttackType.MR, e.getAbility().getGuid());

		} else if (EntityGuid.Resist.getGuid() == e.getMitigation().getGuid()) {
			context.addAttack(AttackType.FT, e.getAbility().getGuid());

		} else {
			// miss, glance, shield ... inconclusive
		}
	}

	public void setCharacterName(final Actor self, final long timestamp) {
		combatLog.setCharacterName(self.getName());

		if (logger.isDebugEnabled()) {
			logger.debug("Player name detected as [" + combatLog.getCharacterName() + "] at " + Event.formatTs(timestamp));
		}

		self.setType(Actor.Type.SELF);
	}

	public void processEventHots(final Event e, final Matcher baseMatcher) {

		final CharacterDiscipline currentDiscipline = (combat == null ? null : actorStates.get(e.getSource()).discipline);
		if (currentDiscipline == null || CharacterDiscipline.Medicine.equals(currentDiscipline)) {
			processEventHotsWithRefresh(e, EntityGuid.KoltoProbe.getGuid(),
					new EntityGuid[]{EntityGuid.SurgicalProbe, EntityGuid.KoltoInfusion}, 17000);
			if (currentDiscipline != null) {
				return;
			}
		}

		if (currentDiscipline == null || CharacterDiscipline.Sawbones.equals(currentDiscipline)) {
			processEventHotsWithRefresh(e, EntityGuid.SlowReleaseMedpac.getGuid(),
					new EntityGuid[]{EntityGuid.EmergencyMedpac, EntityGuid.KoltoPack}, 17000);
			if (currentDiscipline != null) {
				return;
			}
		}

		if (currentDiscipline == null || CharacterDiscipline.Bodyguard.equals(currentDiscipline)) {
			processEventHotsSimple(e, EntityGuid.KoltoShell.getGuid(), null, baseMatcher);
			if (currentDiscipline != null) {
				return;
			}
		}

		if (currentDiscipline == null || CharacterDiscipline.CombatMedic.equals(currentDiscipline)) {
			processEventHotsSimple(e, EntityGuid.TraumaProbe.getGuid(), null, baseMatcher);
			if (currentDiscipline != null) {
				return;
			}
		}

		if (currentDiscipline == null || CharacterDiscipline.Seer.equals(currentDiscipline)) {
			processEventHotsSimple(e, new long[]{
					EntityGuid.ForceArmor.getGuid(),
					EntityGuid.MendingForceArmor.getGuid(),
					EntityGuid.PreservedForceArmor.getGuid(),
					EntityGuid.ImbuedForceArmor.getGuid()}, null, baseMatcher); // 30000);
			if (currentDiscipline != null) {
				return;
			}
		}

		if (currentDiscipline == null || CharacterDiscipline.Corruption.equals(currentDiscipline)) {
			processEventHotsSimple(e, new long[]{
					EntityGuid.StaticBarrier30.getGuid(),
					EntityGuid.StaticBarrier1.getGuid(),
					EntityGuid.StaticBarrier2.getGuid(),
					EntityGuid.StaticBarrier3.getGuid()}, null, baseMatcher); // 30000);
			if (currentDiscipline != null) {
				return;
			}
		}
	}

	private void processEventHotsWithRefresh(final Event e, final long abilityGuid, final EntityGuid[] refreshers, @SuppressWarnings("SameParameterValue") final int defaultDuration) {
		final ActorState targetState = getActorState(e.getTarget());

		if (isAbilityEqual(e, abilityGuid)) {
			if (isEffectAbilityActivate(e)) {
				// lets wait who's the target ...
				pendingHealAbility = e.getAbility();
				return;
			}
//			if (isEffectEqual(e, EntityGuid.ModifyCharges.getGuid())) {
//				// broken now
//			}

			if ((isEffectEqual(e, abilityGuid) && isActionApply(e))
					|| (pendingHealAbility != null && isAbilityEqual(e, pendingHealAbility.getGuid()))) {
				if (isEffectEqual(e, abilityGuid)) {
					// explicit gain = 1 stack
					if (logger.isDebugEnabled() && targetState.hotStacks != 0) {
						logger.debug("Unexpected hot stack " + targetState.hotStacks + " since " + new Date(targetState.hotSince) + " at " + e);
					}
					targetState.hotStacks = 1;
				} else {
					// we got our target!
					// implicit stack increase
					targetState.hotStacks = 2;
				}
				targetState.hotEffect = e.getAbility();
				targetState.hotSince = targetState.hotLast = e.getTimestamp();
				targetState.hotDuration = (hotAverage == 0 ? defaultDuration : hotAverage);
				pendingHealAbility = null;
				return;
			}

			if (isActionRemove(e)) {
				// clear
				if (targetState.hotSince != null) {
					updateHotDurationAverage((int) (e.getTimestamp() - targetState.hotSince), defaultDuration);
				}
				targetState.hotStacks = 0;
				targetState.hotEffect = null;
				targetState.hotSince = null;
				targetState.hotDuration = 0;
				return;
			}

			return;
		}

		for (final EntityGuid refresher : refreshers) {
			if (isAbilityEqual(e, refresher.getGuid())) {
				if (isEffectAbilityActivate(e)) {
					// lets wait who's the target ...
					pendingHealAbility = e.getAbility();
					return;
				}

				if (pendingHealAbility != null && isAbilityEqual(e, pendingHealAbility.getGuid())) {
					// we got our target!
					if (targetState.hotStacks == 2) {
						// implicit stack prolong
						targetState.hotSince = targetState.hotLast = e.getTimestamp();
					}
					pendingHealAbility = null;
				}
				break;
			}
		}
	}

	private void processEventHotsSimple(final Event e, final long[] abilityGuids, @SuppressWarnings("SameParameterValue") final Integer duration, final Matcher baseMatcher) {
		for (long abilityGuid : abilityGuids) {
			if (isAbilityEqual(e, abilityGuid)) {
				processEventHotsSimple(e, abilityGuid, duration, baseMatcher);
			}
		}
	}

	private void processEventHotsSimple(final Event e, final long abilityGuid, @SuppressWarnings("SameParameterValue") final Integer duration, final Matcher baseMatcher) {
		final ActorState targetState = getActorState(e.getTarget());

		if (isAbilityEqual(e, abilityGuid)) {

			if (e.getAction() != null
					&& e.getAction().getGuid() != null
					&& e.getAction().getGuid().equals(EntityGuid.ModifyCharges.getGuid())
					&& isEffectEqual(e, abilityGuid)
					&& baseMatcher.group("Value") != null) {
				if (logger.isDebugEnabled() && targetState.hotSince == null) {
					logger.debug("Continuing hot at " + e);
				}
				targetState.hotEffect = e.getAbility();
				targetState.hotSince = targetState.hotLast = e.getTimestamp();
				targetState.hotDuration = duration;
				try {
					targetState.hotStacks = Integer.parseInt(baseMatcher.group("Value"));
				} catch (Exception ignored) {
				}
				return;
			}

			if (isEffectEqual(e, abilityGuid) && isActionApply(e)) {
				if (logger.isDebugEnabled() && targetState.hotSince != null) {
					logger.debug("Unexpected hot since " + new Date(targetState.hotSince) + " at " + e);
				}
				if (isEffectiveLogged
						&& baseMatcher.group("Value") != null
						&& baseMatcher.group("DamageTypeGuid").equals(EntityGuid.Charges.toString())) {
					try {
						targetState.hotStacks = Integer.parseInt(baseMatcher.group("Value"));
						if (targetState.hotStacks == 6 && (abilityGuid == EntityGuid.KoltoShell.getGuid() || abilityGuid == EntityGuid.TraumaProbe.getGuid())) {
							targetState.hotStacks = 7; // 7.0.0 passive bug
						}
					} catch (Exception ignored) {
						targetState.hotStacks = 0;
					}
				} else {
					targetState.hotStacks = 0;
				}
				targetState.hotEffect = e.getAbility();
				targetState.hotSince = targetState.hotLast = e.getTimestamp();
				targetState.hotDuration = duration;
				return;
			}

			if (isActionRemove(e) && isEffectEqual(e, abilityGuid)) {
				if (logger.isDebugEnabled() && targetState.hotSince == null) {
					logger.debug("Unexpected fade of hot at " + e);
				}
				// clear
				targetState.hotStacks = 0;
				targetState.hotEffect = null;
				targetState.hotSince = null;
				targetState.hotDuration = null;
				return;
			}

			//noinspection UnnecessaryReturnStatement
			return;
		}
	}

	private void updateHotDurationAverage(final int last, final int def) {
		if (hotAverage == 0) {
			hotAverage = def;
			return;
		}
		hotCount++;
		hotTotal += last;
		final double newAverage = (hotTotal * 1.0 / hotCount);
		if (newAverage > hotAverage) {
			hotAverage = (int) Math.min(hotAverage * 1.05, newAverage);
		} else {
			hotAverage = (int) Math.max(hotAverage * 0.98, newAverage);
		}
	}

	private ActorState getActorState(final Actor actor) {
		if (!actorStates.containsKey(actor)) {
			actorStates.put(actor, new ActorState());
		}
		return actorStates.get(actor);
	}

	private void resolveCombatUpgrade(final RaidBoss upgradeBoss, boolean isConfident) {
		if (upgradeBoss == null) {
			// nothing to do, keep current
			return;
		}
		isConfident = isConfident || (Mode.NiM.equals(upgradeBoss.getMode())); // NiM upgrades are always confident
		if (upgradeBoss == combat.getBoss()) {
			// confirmed self
			if (logger.isDebugEnabled()) {
				logger.debug("Boss confirmed as [" + combat.getBoss() + "] at " + eventId);
			}
			combatBossUpgrade = null;

			if (isConfident) {
				instanceMode = upgradeBoss.getMode();
				instanceSize = upgradeBoss.getSize();
			}

		} else {
			// upgraded!
			combat.setBoss(upgradeBoss);

			// another upgrade available?
			if (isConfident) {
				instanceMode = upgradeBoss.getMode();
				instanceSize = upgradeBoss.getSize();
			}
			combatBossUpgrade = upgradeBoss.getPossibleUpgrade();

			if (logger.isDebugEnabled()) {
				logger.debug("Boss upgraded to [" + combat.getBoss() + "] (" + (combatBossUpgrade == null ? "final" : "tentative") + ") at " + eventId);
			}
		}
	}

	private void clearHotsTracking() {
		for (final ActorState as : actorStates.values()) {
			if (as.hotSince != null) {
				as.hotStacks = 0;
				as.hotEffect = null;
				as.hotSince = null;
				as.hotDuration = null;
			}
		}
	}

	// junit
	Context getContext() {
		return context;
	}
}
