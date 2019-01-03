package com.ixale.starparse.serialization;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Output;
import com.ixale.starparse.domain.Actor;
import com.ixale.starparse.domain.CharacterClass;
import com.ixale.starparse.domain.CharacterDiscipline;
import com.ixale.starparse.domain.CharacterRole;
import com.ixale.starparse.domain.Effect;
import com.ixale.starparse.domain.Entity;
import com.ixale.starparse.domain.EntityGroup;
import com.ixale.starparse.domain.EntityGuid;
import com.ixale.starparse.domain.Event;
import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;
import com.ixale.starparse.domain.RaidChallengeName;
import com.ixale.starparse.domain.RaidGroup;
import com.ixale.starparse.domain.RaidRequest;
import com.ixale.starparse.domain.stats.AbsorptionStats;
import com.ixale.starparse.domain.stats.ChallengeStats;
import com.ixale.starparse.domain.stats.CombatEventStats;
import com.ixale.starparse.domain.stats.CombatStats;
import com.ixale.starparse.ws.RaidCombatMessage;
import com.ixale.starparse.ws.RaidCombatMessageBatch;
import com.ixale.starparse.ws.RaidGroupMessage;
import com.ixale.starparse.ws.RaidPlayerMessage;
import com.ixale.starparse.ws.RaidRequestMessage;
import com.ixale.starparse.ws.RaidResponseMessage;
import com.ixale.starparse.ws.ResultMessage;

public class KryoSerialization {

	private static KryoSerialization instance = new KryoSerialization();

	private ThreadLocal<Kryo> kryos;

	KryoSerialization() {
		kryos = new ThreadLocal<Kryo>() {
			protected Kryo initialValue() {
				final Kryo kryo = new Kryo();

				kryo.setRegistrationRequired(true);

				kryo.register(ResultMessage.class);
				kryo.register(ResultMessage.Type.class);
				kryo.register(RaidGroup.class);
				kryo.register(RaidGroupMessage.class);
				kryo.register(RaidGroupMessage.Action.class);
				kryo.register(RaidCombatMessage.class);
				kryo.register(RaidCombatMessage[].class);
				kryo.register(RaidCombatMessageBatch.class);
				kryo.register(CombatStats.class);
				kryo.register(AbsorptionStats.class);
				kryo.register(ChallengeStats.class);
				kryo.register(RaidPlayerMessage.class);
				kryo.register(RaidPlayerMessage.Action.class);

				kryo.register(String[].class);
				kryo.register(ArrayList.class);

				kryo.register(EntityGroup.class);
				kryo.register(EntityGuid.class);
				kryo.register(Raid.class);
				kryo.register(Raid.Mode.class);
				kryo.register(Raid.Size.class);
				kryo.register(RaidBoss.class);
				kryo.register(RaidBossName.class);
				kryo.register(RaidChallengeName.class);

				kryo.register(CharacterRole.class);
				kryo.register(CharacterClass.class);
				kryo.register(CharacterDiscipline.class);
				kryo.register(CombatEventStats.class);

				kryo.register(byte[].class);
				kryo.register(Entity.class);
				kryo.register(Actor.Type.class);
				kryo.register(Actor.class);
				kryo.register(Effect.class);
				kryo.register(Event.class);
				kryo.register(Event.Type.class);

				kryo.register(RaidRequest.Type.class);
				kryo.register(RaidRequest.Params.class);
				kryo.register(RaidRequest.class);
				kryo.register(RaidRequestMessage.class);
				kryo.register(RaidResponseMessage.class);

				kryo.register(Effect.Type.class);

				return kryo;
			};
		};
	}

	public static Kryo getKryo() {
		return instance.kryos.get();
	}

	public static void destroyKryo() {
		instance.kryos.get().reset();
		instance.kryos.remove();
	}

	public static byte[] writeObject(final Object obj) {

		Output output = null;
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			output = new Output(bos);
			getKryo().writeObject(output, obj);
			output.close();

			return bos.toByteArray();

		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (Exception ignore) {
				}
			}
		}
	}

	public static <T> T readObject(final InputStream is, Class<T> clazz) throws Exception {
		ByteBufferInput bis = null;
		try {
			bis = new ByteBufferInput(is);
			return getKryo().readObject(bis, clazz);

		} finally {
			if (bis != null) {
				bis.close();
			}
		}
	}
}
