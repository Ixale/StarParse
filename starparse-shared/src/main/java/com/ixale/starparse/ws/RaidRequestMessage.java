package com.ixale.starparse.ws;

import com.ixale.starparse.domain.RaidRequest;

public class RaidRequestMessage extends BaseMessage {

	private static final long serialVersionUID = 1L;

	String guid;
	RaidRequest request;

	RaidRequestMessage() {
	}

	public RaidRequestMessage(String guid, RaidRequest request) {
		super();
		this.guid = guid;
		this.request = request;
	}

	public String getGuid() {
		return guid;
	}

	public RaidRequest getRequest() {
		return request;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		result = prime * result + ((request == null) ? 0 : request.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RaidRequestMessage other = (RaidRequestMessage) obj;
		if (guid == null) {
			if (other.guid != null)
				return false;
		} else if (!guid.equals(other.guid))
			return false;
		if (request == null) {
			if (other.request != null)
				return false;
		} else if (!request.equals(other.request))
			return false;
		return true;
	}

	public String toString() {
		return request + " (" + guid + ")";
	}
}
