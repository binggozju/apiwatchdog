package org.binggo.apiwatchdog;

public interface TimerRunnable {

	/**
	 * <p>it should be activated by {@link Scheduled} periodically.</p>
	 */
	void runTimerTask();
}
