package com.ixale.starparse.domain;

import com.ixale.starparse.domain.Raid.Mode;
import com.ixale.starparse.domain.Raid.Size;

public class RaidBoss {

	final private RaidBossName name;
	final private Mode mode;
	final private Size size;
	final private long[] tentativeNpcGuids;
	final private long[] confidentNpcGuids;
	final private BossUpgrade upgrade;
	final private Raid raid;

	public static class BossUpgrade {

		protected RaidBoss currentBoss;
		protected final RaidBoss[] upgrades;

		public BossUpgrade(final RaidBoss[] upgrades) {
			this.upgrades = upgrades;
		}

		private void initialize(final RaidBoss currentBoss) {
			this.currentBoss = currentBoss;
		}

		public RaidBoss upgradeByNpc(final long npcGuid) {
			if (currentBoss.confidentNpcGuids != null) {
				for (long guid: currentBoss.confidentNpcGuids) {
					if (guid == npcGuid) {
						// confirmation of self (will stop upgrading)
						return currentBoss;
					}
				}
			}
			if (this.upgrades != null) {
				for (final RaidBoss upgradeBoss: upgrades) {
					if (upgradeBoss.confidentNpcGuids == null) {
						continue;
					}
					for (long guid: upgradeBoss.confidentNpcGuids) {
						if (guid == npcGuid) {
							// upgrade to another mode/size
							return upgradeBoss;
						}
					}
				}
			}
			return null;
		}

		public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
			return null;
		}

		public RaidBoss upgradeByModeAndSize(final Raid.Mode mode, final Raid.Size size) {
			if (this.upgrades != null) {
				for (final RaidBoss upgradeBoss: upgrades) {
					if ((mode == null || upgradeBoss.mode == mode)
						&& (size == null || upgradeBoss.size == size)) {
						// upgrade to another mode/size
						return upgradeBoss;
					}
				}
			}
			return null;
		}
	}

	public interface BossUpgradeCallback {
		public RaidBoss upgradeByAbility(long guid, long effectGuid, Integer value, RaidBoss boss);

		public RaidBoss upgradeByNpc(long guid, RaidBoss boss);
	}

	public RaidBoss(final Raid r, final RaidBossName n, final Mode m, final Size s,
		final long[] cg) {
		this(r, n, m, s, cg, null, null);
	}

	public RaidBoss(final Raid r, final RaidBossName n, final Mode m, final Size s,
		final long[] cg, final long[] tg, final BossUpgrade upgradeCallback) {
		raid = r;
		name = n;
		mode = m;
		size = s;
		confidentNpcGuids = cg;
		tentativeNpcGuids = tg;
		upgrade = upgradeCallback;
		if (upgrade != null) {
			upgrade.initialize(this);
		}
	}

	public Raid getRaid() {
		return raid;
	}

	public Mode getMode() {
		return mode;
	}

	public Size getSize() {
		return size;
	}

	public RaidBossName getRaidBossName() {
		return name;
	}

	public String getName() {
		return name.getFullName();
	}

	public long[] getConfidentNpcGuids() {
		return confidentNpcGuids;
	}

	public long[] getTentativeNpcGuids() {
		return tentativeNpcGuids;
	}

	public BossUpgrade getPossibleUpgrade() {
		return upgrade;
	}

	public static void add(final Raid r, final RaidBossName n,
		final long[] sm8, final long[] sm16,
		final long[] hm8, final long[] hm16) {

		final RaidBoss h16 = new RaidBoss(r, n, Mode.HM, Size.Sixteen, hm16);
		final RaidBoss h8 = new RaidBoss(r, n, Mode.HM, Size.Eight, hm8);
		final RaidBoss s16 = new RaidBoss(r, n, Mode.SM, Size.Sixteen, sm16);
		final RaidBoss s8 = new RaidBoss(r, n, Mode.SM, Size.Eight, sm8);

		r.bosses.add(h16);
		r.bosses.add(h8);
		r.bosses.add(s16);
		r.bosses.add(s8);
	}

	public static void add(final Raid r, final RaidBossName n,
		final long[] sm8, final long[] sm16,
		final long[] hm8, final long[] hm16,
		final BossUpgradeCallback nimUpgrade) {
		// nim
		final RaidBoss n16 = new RaidBoss(r, n, Mode.NiM, Size.Sixteen, null);
		final RaidBoss n8 = new RaidBoss(r, n, Mode.NiM, Size.Eight, null);
		// hm
		final RaidBoss h16 = new RaidBoss(r, n, Mode.HM, Size.Sixteen, null, hm16,
			new RaidBoss.BossUpgrade(new RaidBoss[]{n16}) {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
					return nimUpgrade == null ? null : nimUpgrade.upgradeByAbility(guid, effectGuid, value, n16);
				}
				@Override
				public RaidBoss upgradeByNpc(long npcGuid) {
					final RaidBoss upgrade = (nimUpgrade == null ? null : nimUpgrade.upgradeByNpc(npcGuid, n16));
					return upgrade != null ? upgrade : super.upgradeByNpc(npcGuid);
				}
			});
		final RaidBoss h8 = new RaidBoss(r, n, Mode.HM, Size.Eight, null, hm8,
			new RaidBoss.BossUpgrade(new RaidBoss[]{n8}) {
				@Override
				public RaidBoss upgradeByAbility(final long guid, final long effectGuid, final Integer value) {
					return nimUpgrade == null ? null : nimUpgrade.upgradeByAbility(guid, effectGuid, value, n8);
				}
				@Override
				public RaidBoss upgradeByNpc(long npcGuid) {
					final RaidBoss upgrade = (nimUpgrade == null ? null : nimUpgrade.upgradeByNpc(npcGuid, n8));
					return upgrade != null ? upgrade : super.upgradeByNpc(npcGuid);
				}
			});
		// sm
		final RaidBoss s16 = new RaidBoss(r, n, Mode.SM, Size.Sixteen, sm16);
		final RaidBoss s8 = new RaidBoss(r, n, Mode.SM, Size.Eight, sm8);

		r.bosses.add(n16);
		r.bosses.add(n8);
		r.bosses.add(h16);
		r.bosses.add(h8);
		r.bosses.add(s16);
		r.bosses.add(s8);
	}

	@Override
	public String toString() {
		return name + " (" + mode + " " + size + ")";
	}
}
