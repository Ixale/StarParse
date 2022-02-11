package com.ixale.starparse.ws;


import com.ixale.starparse.domain.RaidGroup;
import com.ixale.starparse.gui.Config;

import javax.websocket.ClientEndpoint;

@ClientEndpoint(encoders = BaseEncoder.class, decoders = BaseDecoder.class, configurator = BaseClientConfigurator.class)
public class RaidGroupClient extends BaseClient {

	public RaidGroupClient(final Config config, final ResultHandler resultHandler) {
		super(config, Utils.ENDPOINT_RAID_GROUP, resultHandler);
	}

	public void manageRaidGroup(final RaidGroup raidGroup, RaidGroupMessage.Action action) {

		sendMessage(new RaidGroupMessage(action, raidGroup), true);
	}

}
