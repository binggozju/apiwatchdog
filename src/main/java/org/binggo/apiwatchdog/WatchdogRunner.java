package org.binggo.apiwatchdog;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class WatchdogRunner implements Runnable {
	
	private AtomicBoolean shouldStop = new AtomicBoolean(false);
	
	public void setShouldStop(Boolean stop) {
		shouldStop.set(stop);
	}
	
	public Boolean shouldStop() {
		return shouldStop.get();
	}
	
	
}
