package com.ixale.starparse.domain;

import java.io.IOException;
import java.io.Serializable;

public class RaidGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String clientPasswordEnc, adminPasswordEnc;

	public RaidGroup() {
		
	}

	public RaidGroup(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@SuppressWarnings("restriction")
	public String getClientPassword() {
		try {
			return new String(new sun.misc.BASE64Decoder().decodeBuffer(this.clientPasswordEnc));
		} catch (IOException e) {
			return null;
		}
	}

	@SuppressWarnings("restriction")
	public void setClientPassword(String clientPassword) {
		this.clientPasswordEnc = new sun.misc.BASE64Encoder().encode(clientPassword.getBytes());
	}

	@SuppressWarnings("restriction")
	public String getAdminPassword() {
		if (this.adminPasswordEnc == null || this.adminPasswordEnc.isEmpty()) {
			return null;
		}
		try {
			return new String(new sun.misc.BASE64Decoder().decodeBuffer(this.adminPasswordEnc));
		} catch (IOException e) {
			return null;
		}
	}

	@SuppressWarnings("restriction")
	public void setAdminPassword(String adminPassword) {
		this.adminPasswordEnc = new sun.misc.BASE64Encoder().encode(adminPassword.getBytes());
	}

	public String toString() {
		return name+" ["+clientPasswordEnc+"]["+adminPasswordEnc+"]";
	}
}
