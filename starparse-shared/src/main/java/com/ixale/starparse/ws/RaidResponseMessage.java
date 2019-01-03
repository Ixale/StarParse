package com.ixale.starparse.ws;

import com.ixale.starparse.domain.RaidRequest;

public class RaidResponseMessage extends BaseMessage {

	private static final long serialVersionUID = 1L;

	String guid;
	RaidRequest.Type requestType;
	byte[] payload;

	RaidResponseMessage() {
	}

	public RaidResponseMessage(String guid, String message) {
		this(guid, message, RaidRequest.Type.SYSTEM_ERROR);
	}

	public RaidResponseMessage(String guid, String message, RaidRequest.Type type) {
		this.requestType = type;
		this.payload = message.getBytes();
		this.guid = guid;
	}

	public RaidResponseMessage(String guid, RaidRequest.Type requestType, byte[] payload) {
		super();
		this.requestType = requestType;
		this.payload = payload;
		this.guid = guid;
	}

	public String getGuid() {
		return guid;
	}

	public RaidRequest.Type getRequestType() {
		return requestType;
	}

	public byte[] getPayload() {
		return payload;
	}

	public String toString() {
		return requestType + " (" + guid + ", " + (payload == null ? "-" : payload.length) + ")";
	}
}
