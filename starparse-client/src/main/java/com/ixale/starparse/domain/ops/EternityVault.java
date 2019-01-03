package com.ixale.starparse.domain.ops;

import com.ixale.starparse.domain.Raid;
import com.ixale.starparse.domain.RaidBoss;
import com.ixale.starparse.domain.RaidBossName;

public class EternityVault extends Raid {

	public EternityVault() {
		super("Eternity Vault");

		RaidBoss.add(this, RaidBossName.AnnihilationDroidXRR3,
			new long[]{1779997656219648L}, // SM 8m
			new long[]{2017165750304768L}, // SM 16m
			new long[]{2034573252755456L}, // HM 8m
			new long[]{2034611907461120L} // HM 16m
		);

		RaidBoss.add(this, RaidBossName.Gharj,
			new long[]{1783772932472832L},
			new long[]{2016946706972672L},
			new long[]{2034526008115200L},
			new long[]{2034534598049792L});

//		RaidBoss.add(this, RaidBossName.AncientPylons,
//			new long[]{1796958482071552L}, // can be any mode, but we don't care for this boss
//			new long[]{2017191520108544L},
//			new long[]{},
//			new long[]{});

		RaidBoss.add(this, RaidBossName.InfernalCouncil,
			new long[]{1977669231050752L, 2098319157362688L, 2098323452329984L, 1977660641116160L, // Kahesh, Jael, Serrod, Yshaar
					2098306272460800L, 1977664936083456L, 2098327747297280L, 2098332042264576L}, // Zaheen, Luthro, Alarii, Fahren
			new long[]{2017230174814208L, 2289642770530304L, 2289647065497600L, 2017238764748800L,
					2289681425235968L, 2017243059716096L, 2289690015170560L, 2289694310137856L,
					2512152141234176L, 2512165026136064L, 2512169321103360L, 2512190795939840L, // Doruk, Kyyrah, Weival, Jekhop
					2512195090907136L, 2512238040580096L, 2512242335547392L, 2512246630514688L}, // Rhoteb, Cyrisop, Keryha, Weival
			new long[]{2289840339025920L, 2289844633993216L, 2289848928960512L, 2289853223927808L,
					2289861813862400L, 2289878993731584L, 2289887583666176L, 2289891878633472L},
			new long[]{2290106626998272L, 2290110921965568L, 2290123806867456L, 2290132396802048L,
					2290136691769344L, 2290140986736640L, 2290145281703936L, 2290149576671232L,
					2512263810383872L, 2512268105351168L, 2512272400318464L, 2512276695285760L,
					2512280990253056L, 2512285285220352L, 2512298170122240L, 2512306760056832L});

		RaidBoss.add(this, RaidBossName.Soa,
			new long[]{1783790112342016L},
			new long[]{2017170045272064L},
			new long[]{2289823159156736L},
			new long[]{2290085152161792L});
	}
}
