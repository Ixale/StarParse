CREATE TABLE logs (
	log_id INT NOT NULL,

	file_name VARCHAR(1000) NOT NULL,
	time_from TIMESTAMP NOT NULL,

	character_name VARCHAR(100) NULL,
 
	PRIMARY KEY (log_id)
);

CREATE TABLE events (
	event_id INT NOT NULL,
	log_id INT NOT NULL,
	timestamp TIMESTAMP NOT NULL,

	source_type TINYINT,
	source_name VARCHAR(50),
	source_guid BIGINT NULL,
	source_instance BIGINT NULL,

	target_type TINYINT,
	target_name VARCHAR(50),
	target_guid BIGINT NULL,
	target_instance BIGINT NULL,
	
	ability_name VARCHAR(50) NULL,
	ability_guid BIGINT NULL,
	
	action_name VARCHAR(50) NULL,
	action_guid BIGINT NULL,

	effect_name VARCHAR(50) NULL,
	effect_guid BIGINT NULL,
	
	value BIGINT NULL,
	is_crit BOOLEAN NULL,

	damage_name VARCHAR(50) NULL,
	damage_guid BIGINT NULL,

	reflect_name VARCHAR(50) NULL,
	reflect_guid BIGINT NULL,

	mitigation_name VARCHAR(50) NULL,
	mitigation_guid BIGINT NULL,

	absorption_name VARCHAR(50) NULL,
	absorption_guid BIGINT NULL,
	absorbed BIGINT NULL,

	threat BIGINT NULL,

	guard_state TINYINT NULL,

	effective_heal BIGINT NULL,
	effective_threat BIGINT NULL,

	PRIMARY KEY (event_id)
);

CREATE TABLE effects (
	effect_id INT NOT NULL,

	time_from TIMESTAMP NOT NULL,
	time_to TIMESTAMP NULL,

	event_id_from INT NOT NULL,
	event_id_to INT NULL,

	is_activated BOOLEAN NOT NULL,
	is_absorption BOOLEAN NOT NULL,
 
	PRIMARY KEY (effect_id)
);

CREATE TABLE absorptions (
	event_id INT NOT NULL,
	effect_id INT NOT NULL,
 
	PRIMARY KEY (event_id)
);

CREATE TABLE combats (
	combat_id INT NOT NULL,
	log_id INT NOT NULL,

	time_from TIMESTAMP NOT NULL,
	time_to TIMESTAMP NULL,

	event_id_from INT NOT NULL,
	event_id_to INT NULL,

	combat_name VARCHAR(255) NULL,
	raid_name VARCHAR(100) NULL,
	boss_name VARCHAR(100) NULL,
	discipline VARCHAR(255) NULL,

	is_running BOOLEAN NOT NULL DEFAULT TRUE,

	is_pvp BOOLEAN NULL,
 
	PRIMARY KEY (combat_id)
);


CREATE TABLE combat_stats (
	combat_id INT NOT NULL,
	player_name VARCHAR(50) NOT NULL,

	time_from TIMESTAMP NOT NULL,
	time_to TIMESTAMP NULL,

	event_id_from INT NOT NULL,
	event_id_to INT NULL,

	discipline VARCHAR(255) NULL,

	-- aggregated
	actions INT NULL,
	apm DOUBLE NULL,

	damage INT NULL,
	dps DOUBLE NULL,

	heal INT NULL,
	hps DOUBLE NULL,
	effective_heal INT NULL,
	ehps DOUBLE NULL,
	ehps_percent DOUBLE NULL,
	effective_heal_taken INT NULL,

	damage_taken INT NULL,
	dtps DOUBLE NULL,

	absorbed INT NULL,
	aps DOUBLE NULL,

	heal_taken INT NULL,
	hps_taken DOUBLE NULL,
	ehps_taken DOUBLE NULL,

	threat INT NULL,
	threat_positive INT NULL,
	tps DOUBLE NULL,

	PRIMARY KEY (combat_id, player_name)
);


CREATE TABLE phases (
	phase_id INT NOT NULL,

	name VARCHAR(255) NULL,
	type VARCHAR(10) NOT NULL,

	combat_id INT NOT NULL,

	event_id_from INT NOT NULL,
	event_id_to INT NULL,

	tick_from INT NOT NULL,
	tick_to INT NULL,

	PRIMARY KEY (phase_id)
);