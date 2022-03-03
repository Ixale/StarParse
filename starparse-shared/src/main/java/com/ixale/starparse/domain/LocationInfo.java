package com.ixale.starparse.domain;

public class LocationInfo {

	private Raid.Mode instanceMode;
	private Raid.Size instanceSize;
	private String instanceName;
	private Long instanceGuid;

	public LocationInfo(final Raid.Mode instanceMode, final Raid.Size instanceSize, final String instanceName, final Long instanceGuid) {
		this.instanceMode = instanceMode;
		this.instanceSize = instanceSize;
		this.instanceName = instanceName;
		this.instanceGuid = instanceGuid;
	}

	public LocationInfo() {
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(final String instanceName) {
		this.instanceName = instanceName;
	}

	public Long getInstanceGuid() {
		return instanceGuid;
	}

	public void setInstanceGuid(final Long instanceGuid) {
		this.instanceGuid = instanceGuid;
	}

	public Raid.Mode getInstanceMode() {
		return instanceMode;
	}

	public void setInstanceMode(final Raid.Mode instanceMode) {
		this.instanceMode = instanceMode;
	}

	public Raid.Size getInstanceSize() {
		return instanceSize;
	}

	public void setInstanceSize(final Raid.Size instanceSize) {
		this.instanceSize = instanceSize;
	}

	public String getInstanceDifficulty() {
		return (instanceMode == null ? "" : instanceMode) + " " + (instanceSize == null ? "" : instanceSize);
	}

}
