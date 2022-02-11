package com.ixale.starparse.domain;

import java.io.Serializable;
import java.util.Base64;

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
			return new String(Base64.getDecoder().decode(this.clientPasswordEnc));
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("restriction")
	public void setClientPassword(String clientPassword) {
		this.clientPasswordEnc = new String(Base64.getEncoder().encode(clientPassword.getBytes()));
	}

	@SuppressWarnings("restriction")
	public String getAdminPassword() {
		if (this.adminPasswordEnc == null || this.adminPasswordEnc.isEmpty()) {
			return null;
		}
		try {
			return new String(Base64.getDecoder().decode(this.adminPasswordEnc));
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("restriction")
	public void setAdminPassword(String adminPassword) {
		this.adminPasswordEnc = new String(Base64.getEncoder().encode(adminPassword.getBytes()));
	}

	public String toString() {
		return name + " [" + clientPasswordEnc + "][" + adminPasswordEnc + "]";
	}
}
