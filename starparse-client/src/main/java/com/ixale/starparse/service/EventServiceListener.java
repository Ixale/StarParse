package com.ixale.starparse.service;

public interface EventServiceListener {

	public void onNewFile() throws Exception;

	public void onNewCombat() throws Exception;

	public void onNewEvents() throws Exception;
}
